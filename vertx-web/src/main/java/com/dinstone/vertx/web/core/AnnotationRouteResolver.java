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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.dinstone.vertx.web.RouteResolver;
import com.dinstone.vertx.web.annotation.BeanParam;
import com.dinstone.vertx.web.annotation.Consumes;
import com.dinstone.vertx.web.annotation.Context;
import com.dinstone.vertx.web.annotation.CookieParam;
import com.dinstone.vertx.web.annotation.Delete;
import com.dinstone.vertx.web.annotation.FormParam;
import com.dinstone.vertx.web.annotation.Get;
import com.dinstone.vertx.web.annotation.HeaderParam;
import com.dinstone.vertx.web.annotation.MatrixParam;
import com.dinstone.vertx.web.annotation.Path;
import com.dinstone.vertx.web.annotation.PathParam;
import com.dinstone.vertx.web.annotation.Post;
import com.dinstone.vertx.web.annotation.Produces;
import com.dinstone.vertx.web.annotation.Put;
import com.dinstone.vertx.web.annotation.QueryParam;
import com.dinstone.vertx.web.annotation.WebHandler;
import com.dinstone.vertx.web.model.ArgType;
import com.dinstone.vertx.web.model.Argument;
import com.dinstone.vertx.web.model.RouteDefinition;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

public class AnnotationRouteResolver extends AbstractRouteResolver implements RouteResolver {

    private final static Logger LOG = LoggerFactory.getLogger(AnnotationRouteResolver.class);

    @Override
    protected List<RouteDefinition> parseRouteDefinitions(Object service) {
        Class<? extends Object> clazz = service.getClass();
        WebHandler webService = getAnnotation(clazz, WebHandler.class);
        if (webService == null) {
            throw new IllegalStateException("without @WebHandler annotation");
        }
        Produces produces = getAnnotation(clazz, Produces.class);
        Consumes consumes = getAnnotation(clazz, Consumes.class);

        List<RouteDefinition> routeDefinitions = new LinkedList<>();
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

                RouteDefinition definition = parseRouteDefinition(webService, produces, consumes, method);
                if (definition != null) {
                    routeDefinitions.add(definition);
                }
            } catch (Throwable e) {
                LOG.warn("parse route definition error by method " + method, e);
            }
        }

        return routeDefinitions;
    }

    private RouteDefinition parseRouteDefinition(WebHandler wh, Produces produces, Consumes consumes, Method method)
            throws Throwable {
        String httpMethod = null;
        String methodPath = null;
        String[] mproduces = null;
        String[] mconsumes = null;
        for (Annotation annotation : getAnnotations(method)) {
            if (annotation instanceof Get) {
                methodPath = ((Get) annotation).value();
                httpMethod = annotation.annotationType().getSimpleName().toUpperCase();
            } else if (annotation instanceof Post) {
                methodPath = ((Post) annotation).value();
                httpMethod = annotation.annotationType().getSimpleName().toUpperCase();
            } else if (annotation instanceof Put) {
                methodPath = ((Put) annotation).value();
                httpMethod = annotation.annotationType().getSimpleName().toUpperCase();
            } else if (annotation instanceof Delete) {
                methodPath = ((Delete) annotation).value();
                httpMethod = annotation.annotationType().getSimpleName().toUpperCase();
            } else if (annotation instanceof Path) {
                methodPath = ((Path) annotation).value();
            } else if (annotation instanceof Produces) {
                mproduces = ((Produces) annotation).value();
            } else if (annotation instanceof Consumes) {
                mconsumes = ((Consumes) annotation).value();
            }
        }

        // http method annotation exist
        if (methodPath != null) {
            List<Argument> methodParameters = parseMethodParameters(method);
            List<Argument> unkowParameters = getUnkownArguments(methodParameters);
            if (unkowParameters.size() > 0) {
                throw new IllegalArgumentException("args has not annotation for " + unkowParameters);
            }

            String[] sproduces = (produces != null ? produces.value() : null);
            String[] sconsumes = (consumes != null ? consumes.value() : null);
            RouteDefinition definition = new RouteDefinition(wh.value(), sproduces, sconsumes, method);
            definition.setMethodPath("".equals(methodPath) ? method.getName() : methodPath);
            definition.setHttpMethod(httpMethod);
            definition.setConsumes(mconsumes);
            definition.setProduces(mproduces);

            definition.setArguments(methodParameters);
            definition.setReturnType(method.getReturnType());

            return definition;
        }

        return null;
    }

    private static List<Annotation> getAnnotations(Method method) {
        List<Annotation> annotationSet = new LinkedList<>();

        // skip static methods
        if (Modifier.isStatic(method.getModifiers())) {
            return annotationSet;
        }
        // skip non public methods
        if (!Modifier.isPublic(method.getModifiers())) {
            return annotationSet;
        }

        // search from method
        collectAnnotationSet(annotationSet, method.getDeclaredAnnotations());

        // search from interface class
        Class<?> clazz = method.getDeclaringClass();
        for (Class<?> iface : clazz.getInterfaces()) {
            try {
                Method equivalentMethod = iface.getDeclaredMethod(method.getName(), method.getParameterTypes());
                collectAnnotationSet(annotationSet, getAnnotations(equivalentMethod).toArray(new Annotation[0]));
            } catch (NoSuchMethodException ex) {
                // Skip this interface - it doesn't have the method...
            }
        }

        // search from super class
        clazz = clazz.getSuperclass();
        if (clazz != null && Object.class != clazz) {
            try {
                Method equivalentMethod = clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
                collectAnnotationSet(annotationSet, getAnnotations(equivalentMethod).toArray(new Annotation[0]));
            } catch (NoSuchMethodException ex) {
                // No equivalent method found
            }
        }
        return annotationSet;
    }

    private static void collectAnnotationSet(List<Annotation> annotationList, Annotation[] annotations) {
        if (annotations.length > 0) {
            for (Annotation annotation : annotations) {
                if (!existAnnotationType(annotationList, annotation)) {
                    annotationList.add(annotation);
                }
            }
        }
    }

    private static boolean existAnnotationType(List<Annotation> annotationList, Annotation annotation) {
        for (Annotation ann : annotationList) {
            if (ann.annotationType() == annotation.annotationType()) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Annotation> T getAnnotation(Class<?> c, Class<T> annotationType) {
        // skip non public classes
        if (!Modifier.isPublic(c.getModifiers())) {
            return null;
        }

        Annotation[] annotations = c.getDeclaredAnnotations();
        for (Annotation ann : annotations) {
            if (ann.annotationType().equals(annotationType)) {
                return (T) ann;
            }
        }

        for (Class<?> ifc : c.getInterfaces()) {
            T annotation = getAnnotation(ifc, annotationType);
            if (annotation != null) {
                return annotation;
            }
        }

        Class<?> superclass = c.getSuperclass();
        if (superclass == null || Object.class == superclass) {
            return null;
        }
        return getAnnotation(superclass, annotationType);
    }

    private List<Argument> parseMethodParameters(Method method) {
        List<Argument> arguments = new ArrayList<>(method.getParameterCount());

        Parameter[] parameters = method.getParameters();
        Class<?>[] paramClazzs = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int argIndex = 0; argIndex < parameterAnnotations.length; argIndex++) {
            String paramName = parameters[argIndex].getName();
            ArgType paramType = ArgType.UNKNOWN;

            for (Annotation annotation : parameterAnnotations[argIndex]) {
                if (annotation instanceof PathParam) {
                    // find path param ... and set index ...
                    paramName = ((PathParam) annotation).value();
                    paramType = ArgType.PATH;
                }

                if (annotation instanceof QueryParam) {
                    // add param
                    paramName = ((QueryParam) annotation).value();
                    paramType = ArgType.QUERY;
                }

                if (annotation instanceof FormParam) {
                    paramType = ArgType.FORM;
                    paramName = ((FormParam) annotation).value();
                }

                if (annotation instanceof CookieParam) {
                    paramType = ArgType.COOKIE;
                    paramName = ((CookieParam) annotation).value();
                }

                if (annotation instanceof HeaderParam) {
                    paramType = ArgType.HEADER;
                    paramName = ((HeaderParam) annotation).value();
                }

                if (annotation instanceof MatrixParam) {
                    paramType = ArgType.MATRIX;
                    paramName = ((MatrixParam) annotation).value();
                }

                if (annotation instanceof BeanParam) {
                    paramType = ArgType.BODY;
                    paramName = parameters[argIndex].getName();
                }

                if (annotation instanceof Context) {
                    paramType = ArgType.CONTEXT;
                    paramName = parameters[argIndex].getName();
                }
            }

            arguments.add(new Argument(argIndex, paramName, paramClazzs[argIndex], paramType));
        }

        if (getUnkownArguments(arguments).size() == 0) {
            return arguments;
        }

        // search from interface class
        Class<?> clazz = method.getDeclaringClass();
        for (Class<?> iface : clazz.getInterfaces()) {
            try {
                Method equivalentMethod = iface.getDeclaredMethod(method.getName(), method.getParameterTypes());
                mergeArguments(arguments, parseMethodParameters(equivalentMethod));
            } catch (NoSuchMethodException ex) {
                // Skip this interface - it doesn't have the method...
            }
        }

        // search from super class
        clazz = clazz.getSuperclass();
        if (clazz != null && Object.class != clazz) {
            try {
                Method equivalentMethod = clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
                mergeArguments(arguments, parseMethodParameters(equivalentMethod));
            } catch (NoSuchMethodException ex) {
                // No equivalent method found
            }
        }

        return arguments;
    }

    private void mergeArguments(List<Argument> supmps, List<Argument> submps) {
        for (int i = 0; i < submps.size(); i++) {
            if (supmps.get(i).getArgType() == ArgType.UNKNOWN && submps.get(i).getArgType() != ArgType.UNKNOWN) {
                supmps.set(i, submps.get(i));
            }
        }
    }

    private List<Argument> getUnkownArguments(List<Argument> methodParameters) {
        List<Argument> unkownMethodParameters = new LinkedList<>();
        for (Argument methodParameter : methodParameters) {
            if (methodParameter.getArgType() == ArgType.UNKNOWN) {
                unkownMethodParameters.add(methodParameter);
            }
        }
        return unkownMethodParameters;
    }

}
