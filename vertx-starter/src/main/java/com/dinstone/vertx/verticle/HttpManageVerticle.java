/*
 * Copyright (C) 2016~2019 dinstone<dinstone@163.com>
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

import com.dinstone.vertx.starter.config.VertxManageProperties;
import com.dinstone.vertx.web.RouterBuilder;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.TimeoutHandler;

public class HttpManageVerticle extends AbstractVerticle {

	private static final Logger LOG = LoggerFactory.getLogger(HttpManageVerticle.class);

	private VertxManageProperties manageProperties;

	private ApplicationContext applicationContext;

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		Router mainRouter = Router.router(vertx);

		mainRouter.route().failureHandler(ErrorHandler.create(false));
		mainRouter.route().handler(LoggerHandler.create());
		mainRouter.route().handler(CookieHandler.create());
		mainRouter.route().handler(BodyHandler.create());
		long timeout = manageProperties.getTimeout();
		if (timeout > 0) {
			mainRouter.route().handler(TimeoutHandler.create(timeout));
		}

		RouterBuilder routerBuilder = RouterBuilder.create(vertx);
		applicationContext.getBeansOfType(Handler.class).forEach((k, v) -> {
			LOG.info("expose endpoint {}:{}", k, v.getClass());
			routerBuilder.handler(v);
		});
		mainRouter.mountSubRouter(getContextPath(), routerBuilder.build());

		HttpServerOptions serverOptions = getRestHttpServerOptions();
		vertx.createHttpServer(serverOptions).requestHandler(mainRouter::accept).listen(ar -> {
			if (ar.succeeded()) {
				LOG.info("start http rest success on {}:{}", serverOptions.getHost(), serverOptions.getPort());
				startFuture.complete();
			} else {
				LOG.error("start http rest failed on {}:{}", serverOptions.getHost(), serverOptions.getPort());
				startFuture.fail(ar.cause());
			}
		});
	}

	private HttpServerOptions getRestHttpServerOptions() {
		HttpServerOptions serverOptions = null;
		try {
			serverOptions = applicationContext.getBean("restHttpServerOptions", HttpServerOptions.class);
		} catch (Exception e) {
			// ignore
		}
		if (serverOptions == null) {
			serverOptions = new HttpServerOptions();
		}

		serverOptions.setIdleTimeout(manageProperties.getIdleTimeout());
		serverOptions.setPort(manageProperties.getPort());

		if (manageProperties.getHost() != null) {
			serverOptions.setHost(manageProperties.getHost());
		}
		return serverOptions;
	}

	private String getContextPath() {
		String contextPath = manageProperties.getContextPath();
		if (contextPath == null || contextPath.isEmpty()) {
			contextPath = "/";
		} else {
			contextPath.trim();
		}

		if (!contextPath.startsWith("/")) {
			contextPath = "/" + contextPath;
		}
		return contextPath;
	}

	public VertxManageProperties getManageProperties() {
		return manageProperties;
	}

	public void setManageProperties(VertxManageProperties manageProperties) {
		this.manageProperties = manageProperties;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

}
