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

package com.dinstone.vertx.web;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.dinstone.vertx.web.annotation.handler.AnnotationHandler;
import com.dinstone.vertx.web.annotation.handler.WebAnnotationHandler;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public interface RouterBuilder {

	class DefaultRouterBuilder implements RouterBuilder {

		private final Router router;

		private final List<AnnotationHandler> handlers = new ArrayList<>();

		public DefaultRouterBuilder(Vertx vertx) {
			this.router = Router.router(vertx);

			handler(new WebAnnotationHandler());
		}

		@Override
		public RouterBuilder route(Object... objs) {
			if (objs != null) {
				for (Object object : objs) {
					process(object);
				}
			}

			return this;
		}

		private void process(Object instance) {
			final Class<?> clazz = instance.getClass();
			for (final Method method : clazz.getMethods()) {
				for (AnnotationHandler annotationHandler : handlers) {
					annotationHandler.process(router, instance, clazz, method);
				}
			}
		}

		@Override
		public Router build() {
			return router;
		}

		@Override
		public RouterBuilder handler(AnnotationHandler handler) {
			if (handler != null) {
				this.handlers.add(handler);
			}
			return this;
		}
	}

	public static RouterBuilder create(Vertx vertx) {
		return new DefaultRouterBuilder(vertx);
	}

	public RouterBuilder handler(AnnotationHandler handler);

	public RouterBuilder route(Object... objs);

	public Router build();
}
