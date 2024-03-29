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
package com.dinstone.vertx.web.converter;

import java.io.IOException;
import java.net.URLDecoder;

import com.dinstone.vertx.web.MessageConverter;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.ParsableHeaderValue;

public class FormMessageConverter implements MessageConverter<MultiMap> {

    private static final String mediaType = "application/x-www-form-urlencoded";

    private static final String defaultCharset = "utf-8";

    @Override
    public String mediaType() {
        return mediaType;
    }

    @Override
    public MultiMap read(Class<? extends MultiMap> clazz, RoutingContext context) throws Exception {
        String charset = null;
        String contentType = context.request().getHeader("Content-Type");
        if (contentType != null) {
            charset = new ParsableHeaderValue(contentType).forceParse().parameter("charset");
        }
        if (charset == null) {
            charset = defaultCharset;
        }

        String[] pairs = context.getBodyAsString(charset).split("&");
        MultiMap result = MultiMap.caseInsensitiveMultiMap();
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx == -1) {
                result.add(URLDecoder.decode(pair, charset), (String) null);
            } else {
                String name = URLDecoder.decode(pair.substring(0, idx), charset);
                String value = URLDecoder.decode(pair.substring(idx + 1), charset);
                result.add(name, value);
            }
        }

        if (clazz.isAssignableFrom(MultiMap.class)) {
            return result;
        }

        return result;
    }

    @Override
    public void write(MultiMap result, RoutingContext context) throws IOException {
        context.response().putHeader("Content-Type", mediaType);

        if (result != null && !result.isEmpty()) {
            
            Buffer buffer = Json.encodeToBuffer(result);
            context.response().end(buffer);
        } else {
            context.response().end();
        }
    }

}
