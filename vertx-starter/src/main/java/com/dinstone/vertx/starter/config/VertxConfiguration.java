package com.dinstone.vertx.starter.config;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;

@Configurable
public class VertxConfiguration {
    class Marker {
    }

    @Bean
    public Marker enableVertxMarker() {
        return new Marker();
    }
}
