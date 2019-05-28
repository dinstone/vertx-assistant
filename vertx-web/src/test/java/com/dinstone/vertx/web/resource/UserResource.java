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
package com.dinstone.vertx.web.resource;

import com.dinstone.vertx.web.annotation.BeanParam;
import com.dinstone.vertx.web.annotation.Context;
import com.dinstone.vertx.web.annotation.FormParam;
import com.dinstone.vertx.web.annotation.Get;
import com.dinstone.vertx.web.annotation.HeaderParam;
import com.dinstone.vertx.web.annotation.PathParam;
import com.dinstone.vertx.web.annotation.Post;
import com.dinstone.vertx.web.annotation.Produces;
import com.dinstone.vertx.web.annotation.QueryParam;
import com.dinstone.vertx.web.annotation.WebHandler;

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;

@WebHandler("/ur")
@Produces("application/json")
public class UserResource {

	@Post("/c/:name")
	public UserBean c(@PathParam("name") String name, @QueryParam("age") int age, @FormParam("sex") boolean sex) {
		return new UserBean(name, age, sex);
	}

	@Post("/bm")
	public UserBean b(@BeanParam MultiMap map, @HeaderParam("Content-Type") String type) {
		System.out.println("Content-Type: " + type);
		return new UserBean(map.get("name"), Integer.parseInt(map.get("age")), Boolean.parseBoolean(map.get("sex")));
	}

	@Post("/bp")
	public int bp(@BeanParam UserBean user) {
		return user.getAge();
	}

	@Post("/bpf")
	public Future<UserBean> bpf(@BeanParam UserBean user) {
		Future<UserBean> future = Future.future();
		future.complete(user);
		return future;
	}

	@Get("/vfn")
	public Void vfn(@Context RoutingContext context) {
		context.response().end("OK");

		return null;
	}

	@Get("/reg/([^\\/]+)/([^\\/]+)")
	public Void reg(@Context RoutingContext context, @PathParam("param0") String name,
			@PathParam("param1") String nick) {
		context.response().end("OK");

		return null;
	}

	@Get("/regp/(?<name>[^\\/]+)/(?<nick>[^\\/]+)")
	public Void regp(@Context RoutingContext context, @PathParam("name") String name, @PathParam("nick") String nick) {
		context.response().end("OK");

		return null;
	}
}
