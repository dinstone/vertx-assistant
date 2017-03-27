package com.dinstone.vertx.web.annotation;

import com.dinstone.vertx.web.RouterBuilder;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public class Example {

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		Router router = RouterBuilder.create(vertx).route(new HelloResource()).build();
		vertx.createHttpServer().requestHandler(router::accept).listen(8080);
		System.out.println("server work on 8080");
	}

}
