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

package com.dinstone.vertx.web.resolver;

import java.lang.annotation.Annotation;
import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.dinstone.vertx.web.RouteResolver;
import com.dinstone.vertx.web.annotation.Connect;
import com.dinstone.vertx.web.annotation.Consumes;
import com.dinstone.vertx.web.annotation.Delete;
import com.dinstone.vertx.web.annotation.Get;
import com.dinstone.vertx.web.annotation.Head;
import com.dinstone.vertx.web.annotation.Options;
import com.dinstone.vertx.web.annotation.Patch;
import com.dinstone.vertx.web.annotation.Path;
import com.dinstone.vertx.web.annotation.Post;
import com.dinstone.vertx.web.annotation.Produces;
import com.dinstone.vertx.web.annotation.Put;

import io.vertx.core.Handler;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class AnnotationRouteResolver implements RouteResolver {

	private static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();

	@Override
	public void process(final Router router, final Object instance, final Class<?> clazz, final Method method) {
		String servicePath = getServicePath(clazz);
		if (isCompatible(method, Get.class, RoutingContext.class)) {
			String methodPath = getAnnotation(method, Get.class).value();
			String path = getRoutePath(servicePath, methodPath, method);
			MethodHandle methodHandle = getMethodHandle(method, RoutingContext.class);
			Route route = router.get(path).handler(wrap(instance, methodHandle));
			andRouteBasedContentType(route, clazz, method);
		}
		if (isCompatible(method, Post.class, RoutingContext.class)) {
			String methodPath = getAnnotation(method, Post.class).value();
			String path = getRoutePath(servicePath, methodPath, method);
			MethodHandle methodHandle = getMethodHandle(method, RoutingContext.class);
			Route route = router.post(path).handler(wrap(instance, methodHandle));
			andRouteBasedContentType(route, clazz, method);
		}
		if (isCompatible(method, Put.class, RoutingContext.class)) {
			String methodPath = getAnnotation(method, Put.class).value();
			String path = getRoutePath(servicePath, methodPath, method);
			MethodHandle methodHandle = getMethodHandle(method, RoutingContext.class);
			Route route = router.put(path).handler(wrap(instance, methodHandle));
			andRouteBasedContentType(route, clazz, method);
		}
		if (isCompatible(method, Delete.class, RoutingContext.class)) {
			String methodPath = getAnnotation(method, Delete.class).value();
			String path = getRoutePath(servicePath, methodPath, method);
			MethodHandle methodHandle = getMethodHandle(method, RoutingContext.class);
			Route route = router.delete(path).handler(wrap(instance, methodHandle));
			andRouteBasedContentType(route, clazz, method);
		}
		if (isCompatible(method, Connect.class, RoutingContext.class)) {
			String methodPath = getAnnotation(method, Connect.class).value();
			String path = getRoutePath(servicePath, methodPath, method);
			MethodHandle methodHandle = getMethodHandle(method, RoutingContext.class);
			Route route = router.connect(path).handler(wrap(instance, methodHandle));
			andRouteBasedContentType(route, clazz, method);
		}
		if (isCompatible(method, Options.class, RoutingContext.class)) {
			String methodPath = getAnnotation(method, Options.class).value();
			String path = getRoutePath(servicePath, methodPath, method);
			MethodHandle methodHandle = getMethodHandle(method, RoutingContext.class);
			Route route = router.options(path).handler(wrap(instance, methodHandle));
			andRouteBasedContentType(route, clazz, method);
		}
		if (isCompatible(method, Head.class, RoutingContext.class)) {
			String methodPath = getAnnotation(method, Head.class).value();
			String path = getRoutePath(servicePath, methodPath, method);
			MethodHandle methodHandle = getMethodHandle(method, RoutingContext.class);
			Route route = router.head(path).handler(wrap(instance, methodHandle));
			andRouteBasedContentType(route, clazz, method);
		}
		if (isCompatible(method, Patch.class, RoutingContext.class)) {
			String methodPath = getAnnotation(method, Patch.class).value();
			String path = getRoutePath(servicePath, methodPath, method);
			MethodHandle methodHandle = getMethodHandle(method, RoutingContext.class);
			Route route = router.patch(path).handler(wrap(instance, methodHandle));
			andRouteBasedContentType(route, clazz, method);
		}
		if (isCompatible(method, Path.class, RoutingContext.class)) {
			String methodPath = getAnnotation(method, Path.class).value();
			String path = getRoutePath(servicePath, methodPath, method);
			MethodHandle methodHandle = getMethodHandle(method, RoutingContext.class);
			Route route = router.route(path).handler(wrap(instance, methodHandle));
			andRouteBasedContentType(route, clazz, method);
		}
	}

	private static String getRoutePath(String servicePath, String methodPath, Method method) {
		if ("".equals(methodPath)) {
			return servicePath + "/" + method.getName();
		}

		if (!methodPath.startsWith("/")) {
			return servicePath + "/" + methodPath;
		}
		return servicePath + methodPath;
	}

	private static void andRouteBasedContentType(Route route, final Class<?> clazz, final Method method) {
		if (route != null) {
			String[] produces = getProduces(clazz, method);
			String[] consumes = getConsumes(clazz, method);
			if (produces != null) {
				for (String contentType : produces) {
					route.produces(contentType);
				}
			}

			if (consumes != null) {
				for (String contentType : consumes) {
					route.consumes(contentType);
				}
			}
		}
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

	private static String[] getConsumes(final Class<?> clazz, final Method method) {
		String[] consumes = null;
		Consumes defaultSetting = getAnnotation(clazz, Consumes.class);
		if (defaultSetting != null) {
			consumes = defaultSetting.value();
		}

		Consumes apiSetting = getAnnotation(method, Consumes.class);
		if (apiSetting != null) {
			consumes = apiSetting.value();
		}
		return consumes;
	}

	private static String[] getProduces(final Class<?> clazz, final Method method) {
		String[] produces = null;
		Produces defaultSetting = getAnnotation(clazz, Produces.class);
		if (defaultSetting != null) {
			produces = defaultSetting.value();
		}

		Produces apiSetting = getAnnotation(method, Produces.class);
		if (apiSetting != null) {
			produces = apiSetting.value();
		}
		return produces;
	}

	private static String getServicePath(final Class<?> clazz) {
		Path routePath = getAnnotation(clazz, Path.class);
		return routePath == null ? "" : routePath.value();
	}

	public static MethodHandle getMethodHandle(Method m, Class<?>... paramTypes) {
		try {
			Class<?>[] methodParamTypes = m.getParameterTypes();

			if (methodParamTypes != null) {
				if (methodParamTypes.length == paramTypes.length) {
					for (int i = 0; i < methodParamTypes.length; i++) {
						if (!paramTypes[i].isAssignableFrom(methodParamTypes[i])) {
							// for groovy and other languages that do not do
							// type check at compile time
							if (!methodParamTypes[i].equals(Object.class)) {
								return null;
							}
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

	public static boolean isCompatible(Method m, Class<? extends Annotation> annotation, Class<?>... paramTypes) {
		if (getAnnotation(m, annotation) != null) {
			if (getMethodHandle(m, paramTypes) != null) {
				return true;
			} else {
				throw new RuntimeException("Method signature not compatible!");
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
}
