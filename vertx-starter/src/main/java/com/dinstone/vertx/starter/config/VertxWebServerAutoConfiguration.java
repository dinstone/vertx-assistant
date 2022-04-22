/*
 * Copyright (C) 2016~2019 dinstone<dinstone@163.com>
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

package com.dinstone.vertx.starter.config;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;

import com.dinstone.vertx.starter.VertxHelper;
import com.dinstone.vertx.verticle.SpringVerticleFactory;
import com.dinstone.vertx.verticle.WebServerVerticle;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

@Configuration
@ConditionalOnBean(VertxWebServerConfiguration.Marker.class)
@EnableConfigurationProperties({ VertxWebServerProperties.class, VertxDefaultProperties.class })
public class VertxWebServerAutoConfiguration {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private VertxWebServerProperties webServerProperties;

    @Autowired
    private VertxDefaultProperties vertxDefaultProperties;

    @Autowired
    private SpringVerticleFactory verticleFactory;

    @Autowired
    private Vertx vertx;

    @EventListener
    public void deployVerticles(ApplicationReadyEvent event) throws Exception {
        DeploymentOptions deployOptions = new DeploymentOptions();
        deployOptions.setInstances(webServerProperties.getInstances());
        String verticleName = verticleFactory.verticleName(WebServerVerticle.class);
        vertx.registerVerticleFactory(verticleFactory);
        VertxHelper.deployVerticle(vertx, deployOptions, verticleName);
    }

    @Bean
    @Scope("prototype")
    public WebServerVerticle webServerVerticle() {
        WebServerVerticle webServerVerticle = new WebServerVerticle();
        webServerVerticle.setApplicationContext(applicationContext);
        webServerVerticle.setWebServerProperties(webServerProperties);
        return webServerVerticle;
    }

    @Bean
    public SpringVerticleFactory verticleFactory() {
        return new SpringVerticleFactory(applicationContext);
    }

    private VertxOptions loadVertxOptions() {
        VertxOptions vertxOptions = new VertxOptions();
        int blockedCheckInterval = vertxDefaultProperties.getBlockedThreadCheckInterval();
        if (blockedCheckInterval > 0) {
            vertxOptions.setBlockedThreadCheckInterval(blockedCheckInterval);
        }

        if (vertxDefaultProperties.getEventLoopPoolSize() > 0) {
            vertxOptions.setEventLoopPoolSize(vertxDefaultProperties.getEventLoopPoolSize());
        }

        if (vertxDefaultProperties.getWorkerPoolSize() > 0) {
            vertxOptions.setWorkerPoolSize(vertxDefaultProperties.getWorkerPoolSize());
        }

        return vertxOptions;
    }

    @PreDestroy
    public void destory() {
        if (vertx != null) {
            vertx.close();
        }
    }

    @Bean
    public Vertx vertx() {
        VertxOptions vertxOptions = null;
        try {
            vertxOptions = applicationContext.getBean(VertxOptions.class);
        } catch (Exception e) {
            // ignore
        }

        if (vertxOptions == null) {
            vertxOptions = loadVertxOptions();
        }
        return Vertx.vertx(vertxOptions);
    }

}
