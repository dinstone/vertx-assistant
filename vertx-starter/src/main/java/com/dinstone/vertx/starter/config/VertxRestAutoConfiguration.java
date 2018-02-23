package com.dinstone.vertx.starter.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import com.dinstone.vertx.starter.VertxHelper;
import com.dinstone.vertx.starter.verticle.HttpRestVerticle;
import com.dinstone.vertx.starter.verticle.SpringVerticleFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

@Configuration
@ConditionalOnClass(EnableVertxRest.class)
@EnableConfigurationProperties(VertxRestProperties.class)
@ComponentScan("com.dinstone.vertx.starter.verticle")
public class VertxRestAutoConfiguration {

    @Autowired
    private Vertx vertx;

    @Autowired
    private VertxRestProperties restProperties;

    @Autowired
    private SpringVerticleFactory verticleFactory;

    @EventListener
    void deployVerticles(ApplicationReadyEvent event) throws Exception {
        DeploymentOptions deployOptions = new DeploymentOptions();
        deployOptions.setInstances(restProperties.getInstances());
        String verticleName = verticleFactory.verticleName(HttpRestVerticle.class);
        VertxHelper.deployVerticle(vertx, deployOptions, verticleFactory, verticleName);
    }

}
