package com.dinstone.vertx.starter.config;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;

@Configurable
public class VertxRestConfiguration {
    class Marker {
    }

    @Bean
    public Marker enableVertxRestMarker() {
        return new Marker();
    }
}
