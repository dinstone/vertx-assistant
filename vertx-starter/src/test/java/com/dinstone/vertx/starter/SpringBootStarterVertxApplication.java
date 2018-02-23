package com.dinstone.vertx.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.dinstone.vertx.starter.config.EnableVertxRest;

@SpringBootApplication
@EnableVertxRest
public class SpringBootStarterVertxApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootStarterVertxApplication.class, args);
    }

    @Bean
    HelloResource create() {
        return new HelloResource();
    }

}
