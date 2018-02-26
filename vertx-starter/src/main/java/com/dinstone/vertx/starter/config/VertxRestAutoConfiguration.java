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

package com.dinstone.vertx.starter.config;

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
import com.dinstone.vertx.starter.verticle.HttpRestVerticle;
import com.dinstone.vertx.starter.verticle.SpringVerticleFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

@Configuration
@ConditionalOnBean(VertxRestConfiguration.Marker.class)
@EnableConfigurationProperties(VertxRestProperties.class)
public class VertxRestAutoConfiguration {

    @Autowired
    private Vertx vertx;

    @Autowired
    private VertxRestProperties restProperties;

    @Autowired
    private SpringVerticleFactory verticleFactory;

    @EventListener
    public void deployVerticles(ApplicationReadyEvent event) throws Exception {
        DeploymentOptions deployOptions = new DeploymentOptions();
        deployOptions.setInstances(restProperties.getInstances());
        String verticleName = verticleFactory.verticleName(HttpRestVerticle.class);
        VertxHelper.deployVerticle(vertx, deployOptions, verticleFactory, verticleName);
    }

    @Bean
    @Scope("prototype")
    public HttpRestVerticle restVerticle(ApplicationContext applicationContext) {
        HttpRestVerticle restVerticle = new HttpRestVerticle();
        restVerticle.setApplicationContext(applicationContext);
        restVerticle.setRestProperties(restProperties);
        return restVerticle;
    }

    @Bean
    public SpringVerticleFactory verticleFactory(ApplicationContext applicationContext) {
        SpringVerticleFactory verticleFactory = new SpringVerticleFactory();
        verticleFactory.setApplicationContext(applicationContext);
        return verticleFactory;
    }

}
