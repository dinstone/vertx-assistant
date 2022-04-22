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
package com.dinstone.vertx.web;

import java.util.ArrayList;
import java.util.List;

import com.dinstone.vertx.web.annotation.WebHandler;
import com.dinstone.vertx.web.core.AnnotationRouteResolver;
import com.dinstone.vertx.web.core.JsonMessageConverter;
import com.dinstone.vertx.web.core.RouterContext;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * builde router for web handler
 * 
 * @author dinstone
 *
 */
public interface RouterBuilder {

	class DefaultRouterBuilder implements RouterBuilder {
		private final List<Object> handlers = new ArrayList<>();
		private final List<RouteResolver> resolvers = new ArrayList<>();
		private final RouterContext routerContext = new RouterContext();

		private Router router;

		public DefaultRouterBuilder(Vertx vertx) {
			router = Router.router(vertx);

			converter(new JsonMessageConverter());
			resolver(new AnnotationRouteResolver());
		}

		@Override
		public RouterBuilder handler(Object handler) {
			if (handler != null) {
				if (handler instanceof ExceptionHandler) {
					routerContext.addExceptionHandler((ExceptionHandler<?>) handler);
				} else {
					handlers.add(handler);
				}
			}

			return this;
		}

		@Override
		public RouterBuilder converter(MessageConverter<?> converter) {
			if (converter != null) {
				routerContext.addMessageConverter(converter);
			}
			return this;
		}

		@Override
		public RouterBuilder resolver(RouteResolver resolver) {
			if (resolver != null) {
				this.resolvers.add(resolver);
			}
			return this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Router build() {
			for (Object handler : handlers) {
				if (handler instanceof Handler) {
					router.route().handler((Handler<RoutingContext>) handler);
					continue;
				}

				for (RouteResolver resolver : resolvers) {
					resolver.resolve(routerContext, router, handler);
				}
			}

			return router;
		}

	}

	public static RouterBuilder create(Vertx vertx) {
		return new DefaultRouterBuilder(vertx);
	}

	/**
	 * and {@link MessageConverter}
	 * 
	 * @param converter
	 * @return
	 */
	public RouterBuilder converter(MessageConverter<?> converter);

	/**
	 * add custome route resolver
	 * 
	 * @param resolver
	 * @return
	 */
	public RouterBuilder resolver(RouteResolver resolver);

	/**
	 * add {@link WebHandler} or {@link ExceptionHandler} or {@link Handler} object
	 * 
	 * @param handler
	 * @return
	 */
	public RouterBuilder handler(Object handler);

	/**
	 * parse route definition and build router
	 * 
	 * @return
	 */
	public Router build();
}
