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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class RouterContext {

    private final ExceptionHandler<Throwable> defaultExceptionHandler = new DefaultExceptionHandler();

    private final Map<String, MessageConverter<?>> messageConverters = new HashMap<>();

    private final Map<Class<?>, ExceptionHandler<?>> exceptionHandlers = new LinkedHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> MessageConverter<T> getMessageConverter(String... mediaTypes) {
        if (mediaTypes != null) {
            for (String mediaType : mediaTypes) {
                MessageConverter<?> converter = messageConverters.get(mediaType);
                if (converter != null) {
                    return (MessageConverter<T>) converter;
                }
            }
        }
        return null;
    }

    public void addMessageConverter(MessageConverter<?>... converters) {
        for (MessageConverter<?> converter : converters) {
            messageConverters.put(converter.mediaType(), converter);
        }
    }

    public void addExceptionHandler(ExceptionHandler<?>... handlers) {
        for (ExceptionHandler<?> exceptionHandler : handlers) {
            Type generic = getGenericType(exceptionHandler.getClass());
            exceptionHandlers.put((Class<?>) generic, exceptionHandler);
        }
    }

    public ExceptionHandler<?> getExceptionHandler(Class<?> t) {
        ExceptionHandler<?> exceptionHandler = exceptionHandlers.get(t);
        if (exceptionHandler != null) {
            return exceptionHandler;
        }

        for (Entry<Class<?>, ExceptionHandler<?>> entry : exceptionHandlers.entrySet()) {
            if (isCompatibleType(t, entry.getKey())) {
                return entry.getValue();
            }
        }

        return defaultExceptionHandler;
    }

    public static Type getGenericType(Class<?> clazz) {
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                Type[] genericTypes = ((ParameterizedType) genericInterface).getActualTypeArguments();
                return genericTypes[0];
            }
        }
    
        return null;
    }

    public static boolean isCompatibleType(Class<?> expected, Type actual) {
        if (expected == null) {
            return false;
        }

        if (actual == null) {
            return true;
        }

        if (actual instanceof ParameterizedType) {
            return expected.isAssignableFrom((Class<?>) ((ParameterizedType) actual).getRawType());
        }

        return expected.equals(actual) || expected.isInstance(actual) || ((Class<?>) actual).isAssignableFrom(expected);
    }
}
