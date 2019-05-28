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

import com.dinstone.vertx.web.annotation.Context;
import com.dinstone.vertx.web.annotation.Get;
import com.dinstone.vertx.web.annotation.WebHandler;

import io.vertx.ext.web.RoutingContext;

@WebHandler("/foo")
public interface FooResource {

    @Get
    public void g(@Context RoutingContext ctx);
}
