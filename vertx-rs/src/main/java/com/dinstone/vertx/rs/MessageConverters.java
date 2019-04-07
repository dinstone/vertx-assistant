package com.dinstone.vertx.rs;

import java.util.HashMap;
import java.util.Map;

import com.dinstone.vertx.rs.resolver.JsonMessageConverter;
import com.dinstone.vertx.rs.resolver.MessageConverter;

public class MessageConverters {

	private final Map<String, MessageConverter<?>> converters = new HashMap<>();

	@SuppressWarnings("unchecked")
	public <T> MessageConverter<T> get(String... mediaTypes) {
		if (mediaTypes != null) {
			for (String mediaType : mediaTypes) {
				MessageConverter<?> converter = converters.get(mediaType);
				if (converter != null) {
					return (MessageConverter<T>) converter;
				}
			}
		}
		return null;
	}

	public void add(String mediaType, JsonMessageConverter converter) {
		converters.put(mediaType, converter);
	}
}
