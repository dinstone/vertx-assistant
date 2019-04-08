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
package com.dinstone.vertx.rs;

import java.util.ArrayList;
import java.util.List;

import com.dinstone.vertx.rs.core.AnnotationRouteResolver;
import com.dinstone.vertx.rs.core.JsonMessageConverter;
import com.dinstone.vertx.rs.core.MessageConverter;
import com.dinstone.vertx.rs.core.RouteResolver;
import com.dinstone.vertx.rs.core.RouterContext;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public interface RouterBuilder {

    class DefaultRouterBuilder implements RouterBuilder {
        private final List<Object> services = new ArrayList<>();
        private final List<RouteResolver> resolvers = new ArrayList<>();
        private final RouterContext routerContext = new RouterContext();

        private Router router;

        public DefaultRouterBuilder(Vertx vertx) {
            router = Router.router(vertx);

            converter(new JsonMessageConverter());
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
        public RouterBuilder converter(MessageConverter<?> converter) {
            if (converter != null) {
                routerContext.add(converter);
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
            for (Object service : services) {
                if (service instanceof Handler) {
                    router.route().handler((Handler<RoutingContext>) service);
                    continue;
                }

                for (RouteResolver resolver : resolvers) {
                    resolver.process(routerContext, router, service);
                }
            }

            return router;
        }

    }

    public static RouterBuilder create(Vertx vertx) {
        return new DefaultRouterBuilder(vertx);
    }

    public RouterBuilder converter(MessageConverter<?> converter);

    public RouterBuilder resolver(RouteResolver resolver);

    public RouterBuilder handler(Object handler);

    public Router build();
}
