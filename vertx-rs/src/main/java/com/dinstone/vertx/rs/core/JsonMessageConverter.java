package com.dinstone.vertx.rs.core;

import java.io.IOException;

import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

public class JsonMessageConverter implements MessageConverter<Object> {

    private String mediaType = "application/json";

    @Override
    public Object read(Class<? extends Object> clazz, RoutingContext context) throws IOException {
        return Json.decodeValue(context.getBody(), clazz);
    }

    @Override
    public void write(Object result, RoutingContext context) throws IOException {
        context.response().putHeader("Content-Type", mediaType);
        context.response().end(Json.encodeToBuffer(result));
    }

    @Override
    public String mediaType() {
        return mediaType;
    }

}
