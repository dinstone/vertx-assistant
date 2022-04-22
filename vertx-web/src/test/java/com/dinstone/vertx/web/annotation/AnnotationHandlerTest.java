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

package com.dinstone.vertx.web.annotation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.dinstone.vertx.web.RouterBuilder;
import com.dinstone.vertx.web.resource.FooImplResource;
import com.dinstone.vertx.web.resource.FooResource;
import com.dinstone.vertx.web.resource.HelloResource;
import com.dinstone.vertx.web.resource.HelloResourceSubclass;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

@RunWith(VertxUnitRunner.class)
public class AnnotationHandlerTest {

    private static Vertx vertx = Vertx.vertx();

    @Test
    public void testHelloResourceGet(TestContext ctx) {
        final Async async = ctx.async();

        final Router router = RouterBuilder.create(vertx).handler(new HelloResource()).build();
        vertx.createHttpServer().requestHandler(router).listen(8081, server -> {
            if (server.failed()) {
                ctx.fail(server.cause());
                return;
            }

            HttpClient httpClient = vertx.createHttpClient();
            httpClient.request(HttpMethod.GET, 8081, "localhost", "/hello/g").onComplete(ar -> {
                if (ar.succeeded()) {
                    HttpClientRequest req = ar.result();
                    req.exceptionHandler(ctx::fail);
                    req.send().onComplete(res -> {
                        ctx.assertTrue(res.succeeded());
                        ctx.assertEquals(200, res.result().statusCode());
                        res.result().bodyHandler(buff -> {
                            ctx.assertEquals("Hello ws!", buff.toString());

                            server.result().close(v -> {
                                if (v.failed()) {
                                    ctx.fail(v.cause());
                                    return;
                                }
                                async.complete();
                            });
                        });
                    });
                } else {
                    ctx.fail(ar.cause());
                }
            });
        });

        async.await();
    }

    @Test
    public void testHelloResourcePost(TestContext ctx) {
        final Async async = ctx.async();

        final Router router = RouterBuilder.create(vertx).handler(new HelloResource()).build();
        vertx.createHttpServer().requestHandler(router).listen(8081, server -> {
            if (server.failed()) {
                ctx.fail(server.cause());
                return;
            }

            HttpClient httpClient = vertx.createHttpClient();
            httpClient.request(HttpMethod.POST, 8081, "localhost", "/hello/p").onComplete(ar -> {
                if (ar.succeeded()) {
                    HttpClientRequest req = ar.result().putHeader("Content-Type", "text/json");
                    req.exceptionHandler(ctx::fail);
                    req.send(new JsonObject().put("content", "ws").toString()).onComplete(res -> {
                        ctx.assertTrue(res.succeeded());
                        ctx.assertEquals(200, res.result().statusCode());
                        ctx.assertEquals("text/plain", res.result().getHeader("Content-Type"));

                        res.result().bodyHandler(buff -> {
                            ctx.assertEquals("Hello ws!", buff.toString());

                            server.result().close(v -> {
                                if (v.failed()) {
                                    ctx.fail(v.cause());
                                    return;
                                }
                                async.complete();
                            });
                        });
                    });
                } else {
                    ctx.fail(ar.cause());
                }
            });
        });

        async.await();
    }

    @Test
    public void testHelloResourceGetPathParam(TestContext ctx) {
        final Async async = ctx.async();

        final Router router = RouterBuilder.create(vertx).handler(new HelloResource()).build();
        vertx.createHttpServer().requestHandler(router).listen(8081, server -> {
            if (server.failed()) {
                ctx.fail(server.cause());
                return;
            }

            HttpClient httpClient = vertx.createHttpClient();
            httpClient.request(HttpMethod.GET, 8081, "localhost", "/hello/g/vert.x").onSuccess(req -> {
                req.exceptionHandler(ctx::fail).send().onComplete(res -> {
                    ctx.assertTrue(res.succeeded());
                    ctx.assertEquals(200, res.result().statusCode());
                    res.result().bodyHandler(buff -> {
                        ctx.assertEquals("hello vert.x", buff.toJsonObject().getString("message"));

                        server.result().close(v -> {
                            if (v.failed()) {
                                ctx.fail(v.cause());
                                return;
                            }
                            async.complete();
                        });
                    });
                });
            }).onFailure(t -> ctx.fail(t));

        });

        async.await();
    }

    @Test
    public void testHelloResourceSubclass(TestContext ctx) {
        final Async async = ctx.async();

        final Router router = RouterBuilder.create(vertx).handler(new HelloResourceSubclass()).build();
        vertx.createHttpServer().requestHandler(router).listen(8081, server -> {
            if (server.failed()) {
                ctx.fail(server.cause());
                return;
            }

            HttpClient httpClient = vertx.createHttpClient();
            httpClient.request(HttpMethod.GET, 8081, "localhost", "/hello/g").onSuccess(req -> {
                req.exceptionHandler(ctx::fail).send().onComplete(res -> {
                    ctx.assertTrue(res.succeeded());
                    ctx.assertEquals(200, res.result().statusCode());
                    res.result().bodyHandler(buff -> {
                        ctx.assertEquals("Hello ws!", buff.toString());

                        server.result().close(v -> {
                            if (v.failed()) {
                                ctx.fail(v.cause());
                                return;
                            }
                            async.complete();
                        });
                    });
                });
            }).onFailure(t -> ctx.fail(t));
        });

        async.await();
    }

    @Test
    public void testFooResourceImpl(TestContext ctx) {
        final Async async = ctx.async();

        final Router router = RouterBuilder.create(vertx).handler(new FooImplResource()).build();
        vertx.createHttpServer().requestHandler(router).listen(8081, server -> {
            if (server.failed()) {
                ctx.fail(server.cause());
                return;
            }

            HttpClient httpClient = vertx.createHttpClient();
            httpClient.request(HttpMethod.GET, 8081, "localhost", "/foo/impl").onSuccess(req -> {
                req.exceptionHandler(ctx::fail).send().onComplete(res -> {
                    ctx.assertTrue(res.succeeded());
                    ctx.assertEquals(200, res.result().statusCode());
                    res.result().bodyHandler(buff -> {
                        ctx.assertEquals("Hello foo!", buff.toString());

                        server.result().close(v -> {
                            if (v.failed()) {
                                ctx.fail(v.cause());
                                return;
                            }
                            async.complete();
                        });
                    });
                });
            }).onFailure(t -> ctx.fail(t));
        });

        async.await();
    }

    @Test
    public void testFooResourceProxy(TestContext ctx) {
        final Async async = ctx.async();

        FooResource handler = (FooResource) Proxy.newProxyInstance(FooResource.class.getClassLoader(),
            new Class<?>[] { FooResource.class }, new InvocationHandler() {

                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    RoutingContext ctx = (RoutingContext) args[0];
                    ctx.response().end("Hello proxy!");
                    return null;
                }
            });

        final Router router = RouterBuilder.create(vertx).handler(handler).build();
        vertx.createHttpServer().requestHandler(router).listen(8081, server -> {
            if (server.failed()) {
                ctx.fail(server.cause());
                return;
            }

            HttpClient httpClient = vertx.createHttpClient();
            httpClient.request(HttpMethod.GET, 8081, "localhost", "/foo/g").onSuccess(req -> {
                req.exceptionHandler(ctx::fail).send().onComplete(res -> {
                    ctx.assertTrue(res.succeeded());
                    ctx.assertEquals(200, res.result().statusCode());
                    res.result().bodyHandler(buff -> {
                        ctx.assertEquals("Hello proxy!", buff.toString());

                        server.result().close(v -> {
                            if (v.failed()) {
                                ctx.fail(v.cause());
                                return;
                            }
                            async.complete();
                        });
                    });
                });
            }).onFailure(t -> ctx.fail(t));
        });

        async.await();
    }

}
