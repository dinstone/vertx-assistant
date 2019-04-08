package com.dinstone.vertx.rs.core;

import java.util.HashMap;
import java.util.Map;

public class RouterContext {

    private final Map<String, MessageConverter<?>> messageConverters = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T> MessageConverter<T> get(String... mediaTypes) {
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

    public void add(MessageConverter<?>... converters) {
        for (MessageConverter<?> converter : converters) {
            messageConverters.put(converter.mediaType(), converter);
        }
    }

    public void add(Class<? extends ExceptionHandler>... handlers) {

    }

    public <T> MessageConverter<T> get(Class<? extends ExceptionHandler> handler) {
        return null;
    }
}
