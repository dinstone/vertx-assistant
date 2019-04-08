package com.dinstone.vertx.rs.resource;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.dinstone.vertx.rs.RouterBuilder;

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
    public void testCreate(TestContext ctx) {
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

}
