
package com.dinstone.vertx.starter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VertxRestConfiguration {

    class Marker {
    }

    @Bean
    public Marker enableVertxRestMarker() {
        return new Marker();
    }

}
