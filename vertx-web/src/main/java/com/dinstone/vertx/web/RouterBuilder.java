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

import com.dinstone.vertx.web.annotation.handler.AnnotationRouteResolver;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public interface RouterBuilder {

    class DefaultRouterBuilder implements RouterBuilder {

        private final List<Object> services = new ArrayList<>();

        private final List<RouteResolver> resolvers = new ArrayList<>();

        private Router router;

        public DefaultRouterBuilder(Vertx vertx) {
            router = Router.router(vertx);
            resolver(new AnnotationRouteResolver());
        }

        @Override
        public RouterBuilder handler(Object service) {
            if (service != null) {
                services.add(service);
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

        @Override
        public Router build() {
            for (Object service : services) {
                process(router, service);
            }

            return router;
        }

        private void process(Router router, Object service) {
            final Class<?> clazz = service.getClass();
            for (final Method method : clazz.getMethods()) {
                for (RouteResolver resolver : resolvers) {
                    resolver.process(router, service, clazz, method);
                }
            }
        }
    }

    public static RouterBuilder create(Vertx vertx) {
        return new DefaultRouterBuilder(vertx);
    }

    public RouterBuilder resolver(RouteResolver resolver);

    public RouterBuilder handler(Object handler);

    public Router build();
}
