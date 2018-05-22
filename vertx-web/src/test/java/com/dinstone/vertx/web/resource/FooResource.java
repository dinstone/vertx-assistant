package com.dinstone.vertx.web.resource;

import com.dinstone.vertx.web.annotation.Get;
import com.dinstone.vertx.web.annotation.Handler;
import com.dinstone.vertx.web.annotation.Path;

import io.vertx.ext.web.RoutingContext;

@Handler
@Path("/foo")
public interface FooResource {

	@Get
	public void g(RoutingContext ctx);
}
