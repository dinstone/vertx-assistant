/*
 * Copyright (C) 2016~2018 dinstone<dinstone@163.com>
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

package com.dinstone.vertx.rs.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.dinstone.vertx.rs.annotation.BeanParam;
import com.dinstone.vertx.rs.annotation.Consumes;
import com.dinstone.vertx.rs.annotation.Context;
import com.dinstone.vertx.rs.annotation.CookieParam;
import com.dinstone.vertx.rs.annotation.Delete;
import com.dinstone.vertx.rs.annotation.FormParam;
import com.dinstone.vertx.rs.annotation.Get;
import com.dinstone.vertx.rs.annotation.HeaderParam;
import com.dinstone.vertx.rs.annotation.MatrixParam;
import com.dinstone.vertx.rs.annotation.PathParam;
import com.dinstone.vertx.rs.annotation.Post;
import com.dinstone.vertx.rs.annotation.Produces;
import com.dinstone.vertx.rs.annotation.Put;
import com.dinstone.vertx.rs.annotation.QueryParam;
import com.dinstone.vertx.rs.annotation.RestService;
import com.dinstone.vertx.rs.model.Argument;
import com.dinstone.vertx.rs.model.ParamType;
import com.dinstone.vertx.rs.model.RouteDefinition;

import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class AnnotationRouteResolver extends AbstractRouteResolver implements RouteResolver {

    private final static Logger log = LoggerFactory.getLogger(AnnotationRouteResolver.class);

    public static List<Annotation> getAnnotation(Method method) {
        List<Annotation> annotationList = new LinkedList<>();

        // skip static methods
        if (Modifier.isStatic(method.getModifiers())) {
            return annotationList;
        }
        // skip non public methods
        if (!Modifier.isPublic(method.getModifiers())) {
            return annotationList;
        }

        Annotation[] annotations = method.getDeclaredAnnotations();
        collectAnnotationSet(annotationList, annotations);

        // search from interface class
        Class<?> clazz = method.getDeclaringClass();
        for (Class<?> iface : clazz.getInterfaces()) {
            try {
                Method equivalentMethod = iface.getDeclaredMethod(method.getName(), method.getParameterTypes());
                collectAnnotationSet(annotationList, getAnnotation(equivalentMethod).toArray(new Annotation[0]));
            } catch (NoSuchMethodException ex) {
                // Skip this interface - it doesn't have the method...
            }
        }

        // search from super class
        clazz = clazz.getSuperclass();
        if (clazz != null && Object.class != clazz) {
            try {
                Method equivalentMethod = clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
                collectAnnotationSet(annotationList, getAnnotation(equivalentMethod).toArray(new Annotation[0]));
            } catch (NoSuchMethodException ex) {
                // No equivalent method found
            }
        }
        return annotationList;
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
    public static <T extends Annotation> T getAnnotation(Method method, Class<T> annotationType) {
        // skip static methods
        if (Modifier.isStatic(method.getModifiers())) {
            return null;
        }
        // skip non public methods
        if (!Modifier.isPublic(method.getModifiers())) {
            return null;
        }

        if (annotationType == null) {
            return null;
        }

        Annotation[] annotations = method.getDeclaredAnnotations();
        for (Annotation ann : annotations) {
            if (ann.annotationType().equals(annotationType)) {
                return (T) ann;
            }
        }

        Class<?> clazz = method.getDeclaringClass();
        T result = searchOnInterfaces(method, annotationType, clazz.getInterfaces());

        while (result == null) {
            clazz = clazz.getSuperclass();
            if (clazz == null || Object.class == clazz) {
                break;
            }

            try {
                Method equivalentMethod = clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
                result = getAnnotation(equivalentMethod, annotationType);
            } catch (NoSuchMethodException ex) {
                // No equivalent method found
            }
            if (result == null) {
                result = searchOnInterfaces(method, annotationType, clazz.getInterfaces());
            }
        }

        return result;
    }

    private static <A extends Annotation> A searchOnInterfaces(Method method, Class<A> annotationType,
            Class<?>... ifcs) {
        A annotation = null;
        for (Class<?> iface : ifcs) {
            if (isInterfaceWithAnnotatedMethods(iface)) {
                try {
                    Method equivalentMethod = iface.getMethod(method.getName(), method.getParameterTypes());
                    annotation = getAnnotation(equivalentMethod, annotationType);
                } catch (NoSuchMethodException ex) {
                    // Skip this interface - it doesn't have the method...
                }
                if (annotation != null) {
                    break;
                }
            }
        }
        return annotation;
    }

    static boolean isInterfaceWithAnnotatedMethods(Class<?> iface) {
        Boolean found = Boolean.FALSE;
        for (Method ifcMethod : iface.getMethods()) {
            try {
                if (ifcMethod.getAnnotations().length > 0) {
                    found = Boolean.TRUE;
                    break;
                }
            } catch (Throwable ex) {
            }
        }
        return found;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T getAnnotation(Class<?> c, Class<T> annotationType) {
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

    @Override
    protected List<RouteDefinition> getRouteDefinitions(Object service) {
        Class<? extends Object> clazz = service.getClass();
        RestService rsa = getAnnotation(clazz, RestService.class);
        if (rsa == null) {
            throw new IllegalStateException("without @RestService annotation");
        }
        Produces produces = getAnnotation(clazz, Produces.class);
        Consumes consumes = getAnnotation(clazz, Consumes.class);

        List<RouteDefinition> routeDefinitions = new LinkedList<>();
        for (final Method method : clazz.getMethods()) {
            try {
                // skip static methods
                if (Modifier.isStatic(method.getModifiers())) {
                    continue;
                }
                // skip non public methods
                if (!Modifier.isPublic(method.getModifiers())) {
                    continue;
                }

                RouteDefinition definition = parseDefinition(rsa, produces, consumes, method);
                if (definition != null) {
                    routeDefinitions.add(definition);
                }
            } catch (Throwable e) {
                log.warn("parse route definition error by method {}", method, e);
            }
        }

        return routeDefinitions;
    }

    private RouteDefinition parseDefinition(RestService rs, Produces produces, Consumes consumes, Method method)
            throws Throwable {
        String httpMethod = null;
        String methodPath = null;
        String[] mproduces = null;
        String[] mconsumes = null;
        for (Annotation annotation : getAnnotation(method)) {
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
            } else if (annotation instanceof Produces) {
                mproduces = ((Produces) annotation).value();
            } else if (annotation instanceof Consumes) {
                mconsumes = ((Consumes) annotation).value();
            }
        }

        // http method annotation exist
        if (httpMethod != null) {
            List<Argument> methodParameters = parseMethodParameter(method);
            List<Argument> unkowParameters = getUnkownMethodParameters(methodParameters);
            if (unkowParameters.size() > 0) {
                throw new IllegalArgumentException("args has not param annotation for " + unkowParameters);
            }

            String[] sproduces = (produces != null ? produces.value() : null);
            String[] sconsumes = (consumes != null ? consumes.value() : null);
            RouteDefinition definition = new RouteDefinition(rs.value(), sproduces, sconsumes, method);
            definition.setMethodPath("".equals(methodPath) ? method.getName() : methodPath);
            definition.setHttpMethod(httpMethod);
            definition.setConsumes(mconsumes);
            definition.setProduces(mproduces);

            definition.setReturnType(method.getReturnType());
            definition.setFutureType(isAsync(method.getReturnType()));

            definition.setMethodParameters(methodParameters);

            return definition;
        }

        return null;
    }

    private boolean isAsync(Class<?> returnType) {
        return returnType.equals(Future.class) || returnType.isInstance(Future.class)
                || Future.class.isAssignableFrom(returnType);
    }

    private List<Argument> parseMethodParameter(Method method) {
        List<Argument> methodParameters = new ArrayList<>(method.getParameterCount());

        Parameter[] parameters = method.getParameters();
        Class<?>[] paramClazzs = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int argIndex = 0; argIndex < parameterAnnotations.length; argIndex++) {
            String paramName = parameters[argIndex].getName();
            ParamType paramType = ParamType.unknown;

            for (Annotation annotation : parameterAnnotations[argIndex]) {
                if (annotation instanceof PathParam) {
                    // find path param ... and set index ...
                    paramName = ((PathParam) annotation).value();
                    paramType = ParamType.path;
                }

                if (annotation instanceof QueryParam) {
                    // add param
                    paramName = ((QueryParam) annotation).value();
                    paramType = ParamType.query;
                }

                if (annotation instanceof FormParam) {
                    paramType = ParamType.form;
                    paramName = ((FormParam) annotation).value();
                }

                if (annotation instanceof CookieParam) {
                    paramType = ParamType.cookie;
                    paramName = ((CookieParam) annotation).value();
                }

                if (annotation instanceof HeaderParam) {
                    paramType = ParamType.header;
                    paramName = ((HeaderParam) annotation).value();
                }

                if (annotation instanceof MatrixParam) {
                    paramType = ParamType.matrix;
                    paramName = ((MatrixParam) annotation).value();
                }

                if (annotation instanceof BeanParam) {
                    paramType = ParamType.body;
                    paramName = parameters[argIndex].getName();
                }

                if (annotation instanceof Context) {
                    paramType = ParamType.context;
                    paramName = parameters[argIndex].getName();
                }
            }

            methodParameters.add(new Argument(paramType, paramName, paramClazzs[argIndex], argIndex));
        }

        if (getUnkownMethodParameters(methodParameters).size() == 0) {
            return methodParameters;
        }

        // search from interface class
        Class<?> clazz = method.getDeclaringClass();
        for (Class<?> iface : clazz.getInterfaces()) {
            try {
                Method equivalentMethod = iface.getDeclaredMethod(method.getName(), method.getParameterTypes());
                mergeMethodParameters(methodParameters, parseMethodParameter(equivalentMethod));
            } catch (NoSuchMethodException ex) {
                // Skip this interface - it doesn't have the method...
            }
        }

        // search from super class
        clazz = clazz.getSuperclass();
        if (clazz != null && Object.class != clazz) {
            try {
                Method equivalentMethod = clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
                mergeMethodParameters(methodParameters, parseMethodParameter(equivalentMethod));
            } catch (NoSuchMethodException ex) {
                // No equivalent method found
            }
        }

        return methodParameters;
    }

    private void mergeMethodParameters(List<Argument> supmps, List<Argument> submps) {
        for (int i = 0; i < submps.size(); i++) {
            if (supmps.get(i).getParamType() == ParamType.unknown
                    && submps.get(i).getParamType() != ParamType.unknown) {
                supmps.set(i, submps.get(i));
            }
        }
    }

    private List<Argument> getUnkownMethodParameters(List<Argument> methodParameters) {
        List<Argument> unkownMethodParameters = new LinkedList<>();
        for (Argument methodParameter : methodParameters) {
            if (methodParameter.getParamType() == ParamType.unknown) {
                unkownMethodParameters.add(methodParameter);
            }
        }
        return unkownMethodParameters;
    }

}
