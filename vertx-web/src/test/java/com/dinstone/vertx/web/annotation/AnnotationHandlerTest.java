/*
 * Copyright (C) 2016~2017 dinstone<dinstone@163.com>
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

package com.dinstone.vertx.web.annotation;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.dinstone.vertx.web.RouterBuilder;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

@RunWith(VertxUnitRunner.class)
public class AnnotationHandlerTest {

	private static Vertx vertx = Vertx.vertx();

	@Path("/hello")
	public static class HelloResource {

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

	@Test
	public void testHelloResourceGet(TestContext ctx) {
		final Async async = ctx.async();
		final Router router = RouterBuilder.create(vertx).route(new HelloResource()).build();
		vertx.createHttpServer().requestHandler(router::accept).listen(8080, server -> {
			if (server.failed()) {
				ctx.fail(server.cause());
				return;
			}

			HttpClient httpClient = vertx.createHttpClient();
			httpClient.get(8080, "localhost", "/hello/g").exceptionHandler(ctx::fail).handler(res -> {
				ctx.assertEquals(200, res.statusCode());
				res.bodyHandler(buff -> {
					ctx.assertEquals("Hello ws!", buff.toString());

					server.result().close(v -> {
						if (v.failed()) {
							ctx.fail(v.cause());
							return;
						}
						async.complete();
					});
				});
			}).end();
		});

		async.await();
	}

	@Test
	public void testHelloResourcePost(TestContext ctx) {
		final Async async = ctx.async();
		final Router router = RouterBuilder.create(vertx).route(new HelloResource()).build();
		vertx.createHttpServer().requestHandler(router::accept).listen(8080, server -> {
			if (server.failed()) {
				ctx.fail(server.cause());
				return;
			}

			HttpClient httpClient = vertx.createHttpClient();
			httpClient.post(8080, "localhost", "/hello/p").putHeader("Content-Type", "text/json")
					.exceptionHandler(ctx::fail).handler(res -> {
						ctx.assertEquals(200, res.statusCode());
						ctx.assertEquals("text/plain", res.getHeader("Content-Type"));

						res.bodyHandler(buff -> {
							ctx.assertEquals("Hello ws!", buff.toString());

							server.result().close(v -> {
								if (v.failed()) {
									ctx.fail(v.cause());
									return;
								}
								async.complete();
							});
						});
					}).end(new JsonObject().put("content", "ws").toString());
		});

		async.await();
	}

}
