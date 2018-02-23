package com.dinstone.vertx.starter.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(VertxRestAutoConfiguration.class)
@EnableVertx
public @interface EnableVertxRest {
}
