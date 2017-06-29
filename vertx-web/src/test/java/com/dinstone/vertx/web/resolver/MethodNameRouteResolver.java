/*
 * Copyright (C) 2016~2017 dinstone<dinstone@163.com>
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

import com.dinstone.vertx.web.RouteResolver;

import io.vertx.core.Handler;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class MethodNameRouteResolver implements RouteResolver {

	private static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();

	@Override
	public void process(Router router, Object instance, Class<?> clazz, Method method) {
		MethodHandle methodHandle = getMethodHandle(method, RoutingContext.class);
		if (methodHandle != null) {
			String servicePath = getServicePath(clazz);
			String path = "/" + servicePath + "/" + getApiPath(method);
			router.route(path).handler(wrap(instance, methodHandle));
			System.out.println("route path : " + path);
		}
	}

	private String getApiPath(Method method) {
		return method.getName();
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
		return clazz.getSimpleName();
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

}
