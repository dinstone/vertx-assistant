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
package com.dinstone.vertx.rs.resolver;

import com.dinstone.vertx.rs.RouterBuilder;
import com.dinstone.vertx.rs.resource.HelloResource;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public class RouteResolverExample {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        RouterBuilder builder = RouterBuilder.create(vertx).resolver(new MethodNameRouteResolver());
        Router apiRouter = builder.handler(new HelloResource()).build();

        Router router = Router.router(vertx).mountSubRouter("/api", apiRouter);
        vertx.createHttpServer().requestHandler(router).listen(8080);

        System.out.println("server work on 8080");
        System.out.println("access url: http://localhost:8080/api/HelloResource/get");
        System.out.println("access url: http://localhost:8080/api/hello/g");
    }

}
