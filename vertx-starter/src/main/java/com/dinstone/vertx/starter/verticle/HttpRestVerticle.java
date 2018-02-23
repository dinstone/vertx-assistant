package com.dinstone.vertx.starter.verticle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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

@Component
@Scope("prototype")
public class HttpRestVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRestVerticle.class);

    @Autowired
    private VertxRestProperties restProperties;

    @Autowired
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

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

}
