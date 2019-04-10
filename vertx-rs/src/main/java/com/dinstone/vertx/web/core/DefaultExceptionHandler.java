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
package com.dinstone.vertx.web.core;

import java.util.LinkedHashMap;
import java.util.Map;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

public class DefaultExceptionHandler implements ExceptionHandler<Throwable> {

    @Override
    public void handle(Throwable t, RoutingContext context) {
        Map<String, Object> res = new LinkedHashMap<>();
        if (t == null) {
            res.put("code", "503");
            res.put("message", "The service is unavailable");
        } else {
            res.put("code", "500");
            res.put("message", t.getClass().getName() + ": " + t.getMessage());
        }

        HttpServerResponse response = context.response();
        response.setStatusCode(500).end(Json.encodeToBuffer(res));
    }

}
