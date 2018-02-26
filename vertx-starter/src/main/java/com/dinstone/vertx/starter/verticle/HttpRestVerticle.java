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

package com.dinstone.vertx.starter.verticle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.dinstone.vertx.starter.config.VertxRestProperties;
import com.dinstone.vertx.web.RouterBuilder;
import com.dinstone.vertx.web.annotation.Handler;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.TimeoutHandler;

public class HttpRestVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRestVerticle.class);

    private VertxRestProperties restProperties;

    private ApplicationContext applicationContext;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Router mainRouter = Router.router(vertx);

        mainRouter.route().failureHandler(ErrorHandler.create(false));
        mainRouter.route().handler(LoggerHandler.create());
        mainRouter.route().handler(BodyHandler.create());
        mainRouter.route().handler(CookieHandler.create());
        mainRouter.route().handler(TimeoutHandler.create(restProperties.getTimeout()));

        RouterBuilder routerBuilder = RouterBuilder.create(vertx);
        applicationContext.getBeansWithAnnotation(Handler.class).forEach((k, v) -> {
            LOG.debug("build rest resource for {}", v.getClass());
            routerBuilder.handler(v);
        });
        mainRouter.mountSubRouter(restProperties.getContext(), routerBuilder.build());

        // http server
        int port = restProperties.getPort();
        String host = restProperties.getHost() == null ? "0.0.0.0" : restProperties.getHost();
        HttpServerOptions serverOptions = new HttpServerOptions().setIdleTimeout(180);
        vertx.createHttpServer(serverOptions).requestHandler(mainRouter::accept).listen(port, host, ar -> {
            if (ar.succeeded()) {
                LOG.info("start http rest success, on {}:{}", host, port);
                startFuture.complete();
            } else {
                LOG.error("start http rest failed, on {}:{}", host, port);
                startFuture.fail(ar.cause());
            }
        });
    }

    public void setRestProperties(VertxRestProperties restProperties) {
        this.restProperties = restProperties;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

}
