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
