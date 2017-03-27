package com.dinstone.vertx.web.annotation;

import io.vertx.ext.web.RoutingContext;

@Path("/hello")
public class HelloResource {

	@Get("/g")
	public void get(RoutingContext ctx) {
		ctx.response().end("Hello ws!");
	}

	@Post("/p")
	@Produces({ "text/plain" })
	@Consumes({ "text/json" })
	public void post(RoutingContext ctx) {
		ctx.request().bodyHandler(rs -> {
			String content = rs.toJsonObject().getString("content");
			ctx.response().end("Hello " + content + "!");
		});
	}
}