package com.dinstone.vertx.rs.resolver;

import java.io.IOException;

import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

public class JsonMessageConverter implements MessageConverter<Object> {

	@Override
	public Object read(Class<? extends Object> clazz, RoutingContext context) throws IOException {
		return Json.decodeValue(context.getBody(), clazz);
	}

	@Override
	public void write(Object result, RoutingContext context) throws IOException {
		context.response().end(Json.encodeToBuffer(result));
	}

}
