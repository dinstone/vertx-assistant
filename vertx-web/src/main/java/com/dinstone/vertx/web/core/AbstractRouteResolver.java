/*
 * Copyright (C) 2016~2022 dinstone<dinstone@163.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dinstone.vertx.web.core;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.dinstone.vertx.web.ExceptionHandler;
import com.dinstone.vertx.web.MessageConverter;
import com.dinstone.vertx.web.RouteResolver;
import com.dinstone.vertx.web.model.Argument;
import com.dinstone.vertx.web.model.RouteDefinition;
import com.dinstone.vertx.web.util.Assert;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.BodyHandler;

public abstract class AbstractRouteResolver implements RouteResolver {

    private final static Logger LOG = LoggerFactory.getLogger(AbstractRouteResolver.class);

    @Override
    public void resolve(RouterContext routerContext, Router router, Object service) {
        List<RouteDefinition> definitions = parseRouteDefinitions(service);
        for (RouteDefinition definition : definitions) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Registering " + definition);
            }

            Route route = null;
            if (definition.pathIsRegex()) {
                route = router.routeWithRegex(definition.getRoutePath());
            } else {
                route = router.route(definition.getRoutePath());
            }
            // http method setting
            if (definition.getHttpMethod() != null) {
                route.method(HttpMethod.valueOf(definition.getHttpMethod()));
            }
            // only register if request with body
            if (definition.hasBody() && definition.getConsumes() != null) {
                for (String item : definition.getConsumes()) {
                    route.consumes(item);
                }
            }
            // produces setting
            if (definition.getProduces() != null) {
                for (String item : definition.getProduces()) {
                    // ignore charset when binding
                    route.produces(item);
                }
            }

            // add BodyHandler in case request has a body
            if (definition.hasBody()) {
                route.handler(BodyHandler.create());
            }

            Handler<RoutingContext> handler;
            if (isVoid(definition.getReturnType())) {
                handler = voidHandler(service, definition, routerContext);
            } else if (isFuture(definition.getReturnType())) {
                handler = futureHandler(service, definition, routerContext);
            } else {
                handler = blockHandler(service, definition, routerContext);
            }

            route.handler(handler);
        }
    }

    protected abstract List<RouteDefinition> parseRouteDefinitions(Object service);

    private static Handler<RoutingContext> voidHandler(final Object service, final RouteDefinition definition,
            RouterContext routerContext) {

        return context -> {
            try {
                Object[] args = prepareArguments(context, definition, routerContext);

                String acceptableContentType = context.getAcceptableContentType();
                if (acceptableContentType != null) {
                    context.response().putHeader("Content-Type", acceptableContentType);
                }

                definition.getMethod().invoke(service, args);
            } catch (Throwable e) {
                handleException(e, context, definition, routerContext);
            }
        };
    }

    private static Handler<RoutingContext> futureHandler(final Object service, final RouteDefinition definition,
            RouterContext routerContext) {

        return context -> {
            try {
                Object[] args = prepareArguments(context, definition, routerContext);
                Object result = definition.getMethod().invoke(service, args);

                Future<?> future = (Future<?>) result;
                // wait for future to complete
                future.onComplete(ar -> {
                    if (ar.succeeded()) {
                        try {
                            handleResponse(future.result(), context, definition, routerContext);
                        } catch (Throwable e) {
                            handleException(e, context, definition, routerContext);
                        }
                    } else {
                        handleException(future.cause(), context, definition, routerContext);
                    }
                });
            } catch (Throwable e) {
                handleException(e, context, definition, routerContext);
            }
        };
    }

    private static Handler<RoutingContext> blockHandler(final Object service, final RouteDefinition definition,
            final RouterContext routerContext) {

        return context -> context.vertx().executeBlocking(promise -> {
            try {
                Object[] args = prepareArguments(context, definition, routerContext);
                Object result = definition.getMethod().invoke(service, args);
                promise.complete(result);
            } catch (Throwable e) {
                promise.fail(e);
            }
        }, false, res -> {
            if (res.succeeded()) {
                try {
                    handleResponse(res.result(), context, definition, routerContext);
                } catch (Throwable e) {
                    handleException(e, context, definition, routerContext);
                }
            } else {
                handleException(res.cause(), context, definition, routerContext);
            }
        });
    }

    private static void handleResponse(Object result, RoutingContext context, RouteDefinition definition,
            RouterContext routerContext) throws Throwable {
        if (!context.response().ended()) {
            MessageConverter<Object> messageConverter = null;
            String contentType = context.getAcceptableContentType();
            if (contentType != null) {
                messageConverter = routerContext.getMessageConverter(contentType);
            } else {
                messageConverter = routerContext.getMessageConverter(definition.getProduces());
            }

            if (messageConverter != null) {
                messageConverter.write(result, context);
            } else {
                throw new IllegalStateException("can't find message converter for " + definition);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void handleException(Throwable e, RoutingContext context, final RouteDefinition definition,
            RouterContext routerContext) {
        if (e instanceof ExecutionException || e instanceof InvocationTargetException) {
            e = e.getCause();
        }
        LOG.error("handling exception for " + definition, e);

        try {
            ExceptionHandler<Throwable> handler = (ExceptionHandler<Throwable>) routerContext
                .getExceptionHandler(e.getClass());
            handler.handle(e, context);
        } catch (Throwable ex) {
            LOG.warn("handled exception failed : " + ex.getMessage(), e);
        }

        if (!context.response().ended()) {
            context.response().setStatusCode(503).end("The service is unavailable, " + e.getMessage());
        }
    }

    private static Object[] prepareArguments(RoutingContext context, RouteDefinition definition,
            RouterContext routerContext) throws Exception {
        List<Argument> parameters = definition.getArguments();
        Object[] arguments = new Object[parameters.size()];
        for (Argument parameter : parameters) {
            switch (parameter.getArgType()) {
                case CONTEXT:
                    arguments[parameter.getParamIndex()] = getContextValue(definition, context, parameter);
                    break;
                case COOKIE:
                    Cookie cookie = context.request().getCookie(parameter.getParamName());
                    arguments[parameter.getParamIndex()] = cookie == null ? null : cookie.getValue();
                case HEADER:
                    arguments[parameter.getParamIndex()] = context.request().getHeader(parameter.getParamName());
                    break;
                case PATH:
                    String pathParam = context.request().getParam(parameter.getParamName());
                    arguments[parameter.getParamIndex()] = convertValue(parameter.getParamClazz(), pathParam);
                    break;
                case QUERY:
                    String queryParam = context.request().getParam(parameter.getParamName());
                    arguments[parameter.getParamIndex()] = convertValue(parameter.getParamClazz(), queryParam);
                    break;
                case FORM:
                    String formParam = context.request().getParam(parameter.getParamName());
                    arguments[parameter.getParamIndex()] = convertValue(parameter.getParamClazz(), formParam);
                    break;
                case MATRIX:
                    String matrixParam = getMatrixParam(context.request(), parameter.getParamName());
                    arguments[parameter.getParamIndex()] = convertValue(parameter.getParamClazz(), matrixParam);
                    break;
                case BODY:
                    arguments[parameter.getParamIndex()] = convertBean(definition, context, parameter, routerContext);
                    break;
                default:
                    break;
            }
        }
        return arguments;
    }

    private static String getMatrixParam(HttpServerRequest request, String paramName) {
        String[] items = request.uri().split(";");
        for (String item : items) {
            String[] nameValue = item.split("=");
            if (nameValue.length == 2 && nameValue[0].equals(paramName)) {
                return nameValue[1];
            }
        }

        return null;
    }

    private static Object convertValue(Class<?> paramClazz, String value) {
        if (value == null) {
            return null;
        }

        if (paramClazz.equals(String.class)) {
            return value;
        }

        // primitive types need to be cast differently
        if (paramClazz.isAssignableFrom(boolean.class) || paramClazz.isAssignableFrom(Boolean.class)) {
            return Boolean.valueOf(value);
        }

        if (paramClazz.isAssignableFrom(byte.class) || paramClazz.isAssignableFrom(Byte.class)) {
            return Byte.valueOf(value);
        }

        if (paramClazz.isAssignableFrom(char.class) || paramClazz.isAssignableFrom(Character.class)) {
            Assert.isTrue(value.length() != 0, "Expected Character but got: null");
            return value.charAt(0);
        }

        if (paramClazz.isAssignableFrom(short.class) || paramClazz.isAssignableFrom(Short.class)) {
            return Short.valueOf(value);
        }

        if (paramClazz.isAssignableFrom(int.class) || paramClazz.isAssignableFrom(Integer.class)) {
            return Integer.valueOf(value);
        }

        if (paramClazz.isAssignableFrom(long.class) || paramClazz.isAssignableFrom(Long.class)) {
            return Long.valueOf(value);
        }

        if (paramClazz.isAssignableFrom(float.class) || paramClazz.isAssignableFrom(Float.class)) {
            return Float.valueOf(value);
        }

        if (paramClazz.isAssignableFrom(double.class) || paramClazz.isAssignableFrom(Double.class)) {
            return Double.valueOf(value);
        }

        return null;
    }

    private static Object convertBean(RouteDefinition definition, RoutingContext context, Argument parameter,
            RouterContext routerContext) throws Exception {
        String contentType = context.request().getHeader("Content-Type");
        MessageConverter<Object> converter = routerContext.getMessageConverter(contentType);
        if (converter == null) {
            converter = routerContext.getMessageConverter(definition.getConsumes());
        }

        if (converter == null) {
            throw new IllegalStateException("no message convert for " + parameter);
        }
        return converter.read(parameter.getParamClazz(), context);
    }

    private static Object getContextValue(RouteDefinition definition, RoutingContext context, Argument parameter) {
        Class<?> paramClazz = parameter.getParamClazz();
        // vert.x context
        if (paramClazz.isAssignableFrom(HttpServerResponse.class)) {
            return context.response();
        }

        if (paramClazz.isAssignableFrom(HttpServerRequest.class)) {
            return context.request();
        }

        if (paramClazz.isAssignableFrom(RoutingContext.class)) {
            return context;
        }

        // internal context / reflection of route definition
        if (paramClazz.isAssignableFrom(RouteDefinition.class)) {
            return definition;
        }

        if (paramClazz.isAssignableFrom(Session.class)) {
            return context.session();
        }

        // browse through context storage
        Object item = context.get(parameter.getParamName());
        if (item != null) {
            return item;
        }

        throw new RuntimeException("can't provide @Context of type: " + paramClazz);
    }

    private static boolean isFuture(Class<?> returnType) {
        return returnType.equals(Future.class) || returnType.isInstance(Future.class)
                || Future.class.isAssignableFrom(returnType);
    }

    private static boolean isVoid(Class<?> returnType) {
        return returnType.equals(Void.TYPE) || returnType.equals(Void.class);
    }

}