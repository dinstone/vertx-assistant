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
package com.dinstone.vertx.verticle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.dinstone.vertx.starter.config.VertxWebServerProperties;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;

public class WebServerVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(WebServerVerticle.class);

    private VertxWebServerProperties webServerProperties;

    private ApplicationContext applicationContext;

    @Override
    public void start(Promise<Void> startFuture) throws Exception {
        Router mainRouter = applicationContext.getBean("webServerRouter", Router.class);
        HttpServerOptions serverOptions = getWebServerOptions();
        vertx.createHttpServer(serverOptions).requestHandler(mainRouter).listen(ar -> {
            if (ar.succeeded()) {
                LOG.info("web server success on {}:{}", serverOptions.getHost(), serverOptions.getPort());
                startFuture.complete();
            } else {
                LOG.error("web server failed on {}:{}", serverOptions.getHost(), serverOptions.getPort());
                startFuture.fail(ar.cause());
            }
        });
    }

    private HttpServerOptions getWebServerOptions() {
        HttpServerOptions serverOptions = null;
        try {
            serverOptions = applicationContext.getBean("webServerOptions", HttpServerOptions.class);
        } catch (Exception e) {
            // ignore
        }
        if (serverOptions == null) {
            serverOptions = new HttpServerOptions();
        }

        serverOptions.setIdleTimeout(webServerProperties.getIdleTimeout());
        serverOptions.setPort(webServerProperties.getPort());

        if (webServerProperties.getHost() != null) {
            serverOptions.setHost(webServerProperties.getHost());
        }
        return serverOptions;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    
    public void setWebServerProperties(VertxWebServerProperties webServerProperties) {
        this.webServerProperties = webServerProperties;
    }


}
