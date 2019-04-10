/*
 * Copyright (C) 2016~2018 dinstone<dinstone@163.com>
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
            httpClient.post(8081, "localhost", "/ur/c/dinstone?age=34")
                    .putHeader("Content-Type", "application/x-www-form-urlencoded").exceptionHandler(ctx::fail)
                    .handler(res -> {
                        ctx.assertEquals(200, res.statusCode());
                        ctx.assertEquals("application/json", res.getHeader("Content-Type"));

                        res.bodyHandler(buff -> {
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
                    }).end("sex=true");
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
            httpClient.post(8081, "localhost", "/ur/bm").putHeader("Content-Type", "application/x-www-form-urlencoded")
                    .exceptionHandler(ctx::fail).handler(res -> {
                        ctx.assertEquals(200, res.statusCode());
                        ctx.assertEquals("application/json", res.getHeader("Content-Type"));

                        res.bodyHandler(buff -> {
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
                    }).end("name=superman&age=34&sex=true");
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
            httpClient.post(8081, "localhost", "/ur/bp").putHeader("Content-Type", "application/json")
                    .exceptionHandler(ctx::fail).handler(res -> {
                        ctx.assertEquals(200, res.statusCode());
                        ctx.assertEquals("application/json", res.getHeader("Content-Type"));

                        res.bodyHandler(buff -> {
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
                    }).end(pj.toBuffer());
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
//            pj.put("high", 1.78);

            HttpClient httpClient = vertx.createHttpClient();
            httpClient.post(8081, "localhost", "/ur/bp").putHeader("Content-Type", "application/json")
                    .exceptionHandler(ctx::fail).handler(res -> {
                        ctx.assertEquals(500, res.statusCode());
//                        ctx.assertEquals("application/json", res.getHeader("Content-Type"));

                        res.bodyHandler(buff -> {
                            String a = buff.toString();
//                            ctx.assertEquals("28", a);

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
            httpClient.post(8081, "localhost", "/ur/bpf").putHeader("Content-Type", "application/json")
                    .exceptionHandler(ctx::fail).handler(res -> {
                        ctx.assertEquals(200, res.statusCode());
                        ctx.assertEquals("application/json", res.getHeader("Content-Type"));

                        res.bodyHandler(buff -> {
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
                    }).end(pj.toBuffer());
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
            long st = System.currentTimeMillis();
            httpClient.get(8081, "localhost", "/ur/vfn").exceptionHandler(ctx::fail).handler(res -> {
                ctx.assertEquals(200, res.statusCode());
                res.bodyHandler(buff -> {
                    ctx.assertEquals("OK", buff.toString());

                    long et = System.currentTimeMillis();
                    System.out.println("reqeust exe take's " + (et - st));

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

}
