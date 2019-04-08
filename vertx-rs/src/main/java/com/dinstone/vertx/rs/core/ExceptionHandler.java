package com.dinstone.vertx.rs.core;

import io.vertx.ext.web.RoutingContext;

public interface ExceptionHandler<T extends Throwable> {

    void handle(T t, RoutingContext context);
}
