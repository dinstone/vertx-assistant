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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

@Configurable
@ConditionalOnClass(EnableVertx.class)
@EnableConfigurationProperties(VertxProperties.class)
public class VertxAutoConfiguration {

    @Autowired
    private VertxProperties vertxProperties;

    private Vertx vertx;

    @PostConstruct
    public void init() {
        vertx = Vertx.vertx(loadVertxOptions());
    }

    private VertxOptions loadVertxOptions() {
        VertxOptions vertxOptions = new VertxOptions();
        int blockedCheckInterval = vertxProperties.getBlockedThreadCheckInterval();
        if (blockedCheckInterval > 0) {
            vertxOptions.setBlockedThreadCheckInterval(blockedCheckInterval);
        }

        // JsonObject config = vertxConfig.getJsonObject("vertx.cluster");
        // if (config != null && config.getString("type") != null) {
        // ClusterManager clusterManager = null;
        //
        // String type = config.getString("type");
        // if ("zookeeper".equalsIgnoreCase(type)) {
        // clusterManager = new ZookeeperClusterManager(config);
        // }
        //
        // if (clusterManager != null) {
        // vertxOptions.setClustered(true).setClusterManager(clusterManager);
        // } else {
        // LOG.warn("unkown cluster type [{}]", type);
        // }
        // }

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
        return vertx;
    }
}
