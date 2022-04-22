/*
 * Copyright (C) 2016~2022 dinstone<dinstone@163.com>
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
package com.dinstone.vertx.web.resource;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.dinstone.vertx.web.RouterBuilder;
import com.dinstone.vertx.web.converter.FormMessageConverter;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;

@RunWith(VertxUnitRunner.class)
public class UserResourceTest {

    @Test
    public void testCreate00(TestContext ctx) {
        final Async async = ctx.async();

        Vertx vertx = Vertx.vertx();
        final Router router = RouterBuilder.create(vertx).handler(new UserResource()).build();
        vertx.createHttpServer().requestHandler(router::handle).listen(8081, server -> {
            if (server.failed()) {
                ctx.fail(server.cause());
                return;
            }

            HttpClient httpClient = vertx.createHttpClient();
            httpClient.request(HttpMethod.POST, 8081, "localhost", "/ur/c/dinstone?age=34").onSuccess(req -> {
                req.exceptionHandler(ctx::fail).putHeader("Content-Type", "application/x-www-form-urlencoded")
                    .send("sex=true").onComplete(res -> {
                        ctx.assertTrue(res.succeeded());
                        ctx.assertEquals(200, res.result().statusCode());
                        ctx.assertEquals("application/json; charset=UTF-8", res.result().getHeader("Content-Type"));

                        res.result().bodyHandler(buff -> {
                            JsonObject uo = buff.toJsonObject();
                            ctx.assertEquals("dinstone", uo.getString("name"));
                            ctx.assertEquals(34, uo.getInteger("age"));
                            ctx.assertEquals(true, uo.getBoolean("sex"));

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

        vertx.close();
    }

    @Test
    public void testCreate01(TestContext ctx) {
        final Async async = ctx.async();

        Vertx vertx = Vertx.vertx();
        final Router router = RouterBuilder.create(vertx).handler(new UserResource())
            .converter(new FormMessageConverter()).build();
        vertx.createHttpServer().requestHandler(router::handle).listen(8081, server -> {
            if (server.failed()) {
                ctx.fail(server.cause());
                return;
            }

            HttpClient httpClient = vertx.createHttpClient();
            httpClient.request(HttpMethod.POST, 8081, "localhost", "/ur/bm").onSuccess(req -> {
                req.exceptionHandler(ctx::fail).putHeader("Content-Type", "application/x-www-form-urlencoded")
                    .send("name=superman&age=34&sex=true").onComplete(res -> {
                        ctx.assertEquals(200, res.result().statusCode());
                        ctx.assertEquals("application/json; charset=UTF-8", res.result().getHeader("Content-Type"));

                        res.result().bodyHandler(buff -> {
                            JsonObject uo = buff.toJsonObject();
                            ctx.assertEquals("superman", uo.getString("name"));
                            ctx.assertEquals(34, uo.getInteger("age"));
                            ctx.assertEquals(true, uo.getBoolean("sex"));

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

        vertx.close();
    }

    @Test
    public void testCreate02(TestContext ctx) {
        final Async async = ctx.async();

        Vertx vertx = Vertx.vertx();
        final Router router = RouterBuilder.create(vertx).handler(new UserResource())
            .converter(new FormMessageConverter()).build();
        vertx.createHttpServer().requestHandler(router::handle).listen(8081, server -> {
            if (server.failed()) {
                ctx.fail(server.cause());
                return;
            }

            JsonObject pj = new JsonObject();
            pj.put("name", "akala");
            pj.put("age", 28);
            pj.put("sex", false);

            HttpClient httpClient = vertx.createHttpClient();
            httpClient.request(HttpMethod.POST, 8081, "localhost", "/ur/bp").onSuccess(req -> {
                req.exceptionHandler(ctx::fail).putHeader("Content-Type", "application/json").send(pj.toBuffer())
                    .onComplete(res -> {
                        ctx.assertEquals(200, res.result().statusCode());
                        ctx.assertEquals("application/json; charset=UTF-8", res.result().getHeader("Content-Type"));

                        res.result().bodyHandler(buff -> {
                            String a = buff.toString();
                            ctx.assertEquals("28", a);

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

        vertx.close();
    }

    @Test
    public void testCreate03(TestContext ctx) {
        final Async async = ctx.async();

        Vertx vertx = Vertx.vertx();
        final Router router = RouterBuilder.create(vertx).handler(new UserResource())
            .converter(new FormMessageConverter()).build();
        vertx.createHttpServer().requestHandler(router::handle).listen(8081, server -> {
            if (server.failed()) {
                ctx.fail(server.cause());
                return;
            }

            JsonObject pj = new JsonObject();
            pj.put("name", "akala");
            pj.put("age", 28);
            pj.put("sex", false);
            // pj.put("high", 1.78);

            HttpClient httpClient = vertx.createHttpClient();
            httpClient.request(HttpMethod.POST, 8081, "localhost", "/ur/bp").onSuccess(req -> {
                req.exceptionHandler(ctx::fail).putHeader("Content-Type", "application/json").send().onComplete(res -> {
                    ctx.assertEquals(500, res.result().statusCode());
                    // ctx.assertEquals("application/json", res.getHeader("Content-Type"));

                    res.result().bodyHandler(buff -> {
                        String a = buff.toString();
                        // ctx.assertEquals("28", a);

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

        vertx.close();
    }

    @Test
    public void testCreate04(TestContext ctx) {
        final Async async = ctx.async();

        Vertx vertx = Vertx.vertx();
        final Router router = RouterBuilder.create(vertx).handler(new UserResource())
            .converter(new FormMessageConverter()).build();
        vertx.createHttpServer().requestHandler(router::handle).listen(8081, server -> {
            if (server.failed()) {
                ctx.fail(server.cause());
                return;
            }

            JsonObject pj = new JsonObject();
            pj.put("name", "akala");
            pj.put("age", 28);
            pj.put("sex", false);

            HttpClient httpClient = vertx.createHttpClient();
            httpClient.request(HttpMethod.POST, 8081, "localhost", "/ur/bpf").onSuccess(req -> {
                req.exceptionHandler(ctx::fail).putHeader("Content-Type", "application/json").send(pj.toBuffer())
                    .onComplete(res -> {
                        ctx.assertEquals(200, res.result().statusCode());
                        ctx.assertEquals("application/json; charset=UTF-8", res.result().getHeader("Content-Type"));

                        res.result().bodyHandler(buff -> {
                            JsonObject uo = buff.toJsonObject();
                            ctx.assertEquals("akala", uo.getString("name"));
                            ctx.assertEquals(28, uo.getInteger("age"));
                            ctx.assertEquals(false, uo.getBoolean("sex"));

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

        vertx.close();
    }

    @Test
    public void testGet00(TestContext ctx) {
        final Async async = ctx.async();

        Vertx vertx = Vertx.vertx();
        long s = System.currentTimeMillis();
        final Router router = RouterBuilder.create(vertx).handler(new UserResource()).build();
        long e = System.currentTimeMillis();
        System.out.println("router build take's " + (e - s));
        vertx.createHttpServer().requestHandler(router).listen(8081, server -> {
            if (server.failed()) {
                ctx.fail(server.cause());
                return;
            }

            HttpClient httpClient = vertx.createHttpClient();
            httpClient.request(HttpMethod.GET, 8081, "localhost", "/ur/vfn").onSuccess(req -> {
                req.exceptionHandler(ctx::fail).send().onComplete(res -> {
                    ctx.assertEquals(200, res.result().statusCode());
                    res.result().bodyHandler(buff -> {
                        ctx.assertEquals("OK", buff.toString());

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
    public void testGet01(TestContext ctx) {
        final Async async = ctx.async();

        Vertx vertx = Vertx.vertx();
        long s = System.currentTimeMillis();
        final Router router = RouterBuilder.create(vertx).handler(new UserResource()).build();
        long e = System.currentTimeMillis();
        System.out.println("router build take's " + (e - s));
        vertx.createHttpServer().requestHandler(router).listen(8081, server -> {
            if (server.failed()) {
                ctx.fail(server.cause());
                return;
            }

            HttpClient httpClient = vertx.createHttpClient();
            httpClient.request(HttpMethod.GET, 8081, "localhost", "/ur/reg/gjf/dinstone").onSuccess(req -> {
                req.exceptionHandler(ctx::fail).send().onComplete(res -> {
                    ctx.assertEquals(200, res.result().statusCode());
                    res.result().bodyHandler(buff -> {
                        ctx.assertEquals("OK", buff.toString());

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
    public void testGet02(TestContext ctx) {
        final Async async = ctx.async();

        Vertx vertx = Vertx.vertx();
        long s = System.currentTimeMillis();
        final Router router = RouterBuilder.create(vertx).handler(new UserResource()).build();
        long e = System.currentTimeMillis();
        System.out.println("router build take's " + (e - s));
        vertx.createHttpServer().requestHandler(router).listen(8081, server -> {
            if (server.failed()) {
                ctx.fail(server.cause());
                return;
            }

            HttpClient httpClient = vertx.createHttpClient();
            httpClient.request(HttpMethod.GET, 8081, "localhost", "/ur/regp/gjf/dinstone").onSuccess(req -> {
                req.exceptionHandler(ctx::fail).send().onComplete(res -> {
                    ctx.assertEquals(200, res.result().statusCode());
                    res.result().bodyHandler(buff -> {
                        ctx.assertEquals("OK", buff.toString());

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
