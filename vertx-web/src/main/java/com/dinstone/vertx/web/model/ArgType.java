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

package com.dinstone.vertx.web.model;

/**
 * argument type
 */
public enum ArgType {

    /**
     * unknown type
     */
    UNKNOWN(""),

    /**
     * Http path parameter
     */
    PATH("@PathParam"),

    /**
     * Http query parameter
     */
    QUERY("@QueryParam"),

    /**
     * Cookie in request
     */
    COOKIE("@CookieParam"),

    /**
     * Form parameter
     */
    FORM("@FormParam"),

    /**
     * Request header
     */
    HEADER("@HeaderParam"),

    /**
     * Matrix parameter
     */
    MATRIX("@MatrixParam"),

    /**
     * Request body
     */
    BODY("@BeanParam"),

    /**
     * Any Vert.x available context
     */
    CONTEXT("@Context");

    private final String description;

    ArgType(String value) {
        description = value;
    }

    public String getDescription() {
        return description;
    }
}
