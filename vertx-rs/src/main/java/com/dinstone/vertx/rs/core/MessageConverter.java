package com.dinstone.vertx.rs.core;

import java.io.IOException;

import io.vertx.ext.web.RoutingContext;

public interface MessageConverter<T> {

    public String mediaType();

    public T read(Class<? extends T> clazz, RoutingContext context) throws IOException;

    void write(T result, RoutingContext context) throws IOException;
}
