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
package com.dinstone.vertx.rs.core;

import java.io.IOException;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

public class JsonMessageConverter implements MessageConverter<Object> {

    private String mediaType = "application/json";

    @Override
    public Object read(Class<? extends Object> clazz, RoutingContext context) throws IOException {
        Buffer body = context.getBody();
        if (body.length() > 0) {
            return Json.decodeValue(body, clazz);
        }
        return null;
    }

    @Override
    public void write(Object result, RoutingContext context) throws IOException {
        context.response().putHeader("Content-Type", mediaType);

        if (result != null) {
            Buffer buffer = Json.encodeToBuffer(result);
            context.response().end(buffer);
        } else {
            context.response().end();
        }
    }

    @Override
    public String mediaType() {
        return mediaType;
    }

}
