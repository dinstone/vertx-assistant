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
package com.dinstone.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.dinstone.vertx.starter.EnableVertxWebServer;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

@SpringBootApplication
@EnableVertxWebServer
@Configuration
public class VertxStarterApplication {

    public static void main(String[] args) {
        SpringApplication.run(VertxStarterApplication.class, args);
    }

    @Bean
    @Lazy
    public Router webServerRouter(ApplicationContext applicationContext) {
        Router r = Router.router(applicationContext.getBean(Vertx.class));

        r.route("/health").handler(rc -> {
            rc.response().send("ok");
        });
        r.route().handler(StaticHandler.create().setCachingEnabled(false).setIndexPage("index.html"));

        return r;
    }
}
