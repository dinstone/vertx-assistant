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

package com.dinstone.vertx.web.resolver;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.dinstone.vertx.web.core.AbstractRouteResolver;
import com.dinstone.vertx.web.model.ArgType;
import com.dinstone.vertx.web.model.Argument;
import com.dinstone.vertx.web.model.RouteDefinition;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;

public class MethodNameRouteResolver extends AbstractRouteResolver {

    private static final Logger LOG = LoggerFactory.getLogger(MethodNameRouteResolver.class);

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();

    // @Override
    // public void resolve(RouterContext routerContext, Router router, Object service) {
    // Class<? extends Object> clazz = service.getClass();
    // for (final Method method : clazz.getMethods()) {
    // try {
    // // skip static methods
    // int modifiers = method.getModifiers();
    // if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers) || Modifier.isNative(modifiers)) {
    // continue;
    // }
    //
    // if (method.getDeclaringClass().isInstance(Object.class)) {
    // continue;
    // }
    //
    // String methodName = method.getName();
    // if ("equals".equals(methodName) || "hashCode".equals(methodName) || "toString".equals(methodName)) {
    // continue;
    // }
    //
    // process(router, service, clazz, method);
    // } catch (Throwable e) {
    // LOG.warn("parse route definition error by method " + method, e);
    // }
    // }
    // }

    public void process(Router router, Object instance, Class<?> clazz, Method method) {
        MethodHandle methodHandle = getMethodHandle(method, RoutingContext.class);
        if (methodHandle != null) {
            String path = getServicePath(clazz) + getMehtodPath(method);
            router.route(path).handler(wrap(instance, methodHandle));
            System.out.println("route path : " + path);
        }
    }

    private String getMehtodPath(Method method) {
        return "/" + method.getName();
    }

    public static MethodHandle getMethodHandle(Method m, Class<?>... paramTypes) {
        try {
            if (Modifier.isStatic(m.getModifiers())) {
                return null;
            }
            if (!Modifier.isPublic(m.getModifiers())) {
                return null;
            }

            Class<?>[] methodParamTypes = m.getParameterTypes();

            if (methodParamTypes != null) {
                if (methodParamTypes.length == paramTypes.length) {
                    for (int i = 0; i < methodParamTypes.length; i++) {
                        if (!paramTypes[i].isAssignableFrom(methodParamTypes[i])) {
                            return null;
                        }
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }

            MethodHandle methodHandle = LOOKUP.unreflect(m);
            CallSite callSite = new ConstantCallSite(methodHandle);
            return callSite.dynamicInvoker();

        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private String getServicePath(Class<?> clazz) {
        return "/" + clazz.getSimpleName();
    }

    private static Handler<RoutingContext> wrap(final Object instance, final MethodHandle mh) {
        return ctx -> {
            try {
                String acceptableContentType = ctx.getAcceptableContentType();
                if (acceptableContentType != null) {
                    ctx.response().putHeader("Content-Type", acceptableContentType);
                }

                mh.invoke(instance, ctx);
            } catch (Throwable e) {
                ctx.fail(e);
            }
        };
    }

    @Override
    protected List<RouteDefinition> parseRouteDefinitions(Object service) {
        List<RouteDefinition> routeDefinitions = new LinkedList<>();
        Class<? extends Object> clazz = service.getClass();
        for (final Method method : clazz.getMethods()) {
            try {
                // skip static methods
                int modifiers = method.getModifiers();
                if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers) || Modifier.isNative(modifiers)) {
                    continue;
                }

                if (method.getDeclaringClass().isInstance(Object.class)) {
                    continue;
                }

                String methodName = method.getName();
                if ("equals".equals(methodName) || "hashCode".equals(methodName) || "toString".equals(methodName)) {
                    continue;
                }

                RouteDefinition definition = new RouteDefinition(getServicePath(clazz), null, null, method);
                definition.setMethodPath(getMehtodPath(method));
                definition.setArguments(getArguments(method));
                definition.setReturnType(method.getReturnType());

                routeDefinitions.add(definition);
            } catch (Throwable e) {
                LOG.warn("parse route definition error by method " + method, e);
            }
        }

        return routeDefinitions;
    }

    private List<Argument> getArguments(Method method) {
        List<Argument> arguments = new ArrayList<>(method.getParameterCount());

        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < method.getParameterCount(); i++) {
            Class<?> paramClazz = parameters[i].getType();
            if (paramClazz.isAssignableFrom(RoutingContext.class)
                    || paramClazz.isAssignableFrom(HttpServerResponse.class)
                    || paramClazz.isAssignableFrom(HttpServerRequest.class)
                    || paramClazz.isAssignableFrom(RouteDefinition.class)
                    || paramClazz.isAssignableFrom(Session.class)) {
                arguments.add(new Argument(i, parameters[i].getName(), parameters[i].getType(), ArgType.CONTEXT));
            }
        }

        return arguments;
    }

}
