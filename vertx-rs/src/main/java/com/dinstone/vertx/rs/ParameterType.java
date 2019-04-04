package com.dinstone.vertx.rs;

/**
 *
 */
public enum ParameterType {

    /**
     * Placeholder until we know ... otherwise error is thrown
     */
    unknown(""),

    /**
     * REST path parameter
     */
    path("@PathParam"),

    /**
     * Rest query parameter
     */
    query("@QueryParam"),

    /**
     * Cookie in request
     */
    cookie("@CookieParam"),

    /**
     * Form parameter
     */
    form("@FormParam"),

    /**
     * Request header
     */
    header("@HeaderParam"),

    /**
     * Matrix parameter
     */
    matrix("@MatrixParam"),

    /**
     * Request body
     */
    body("@BeanParam"),

    /**
     * Any Vert.x available context ...
     */
    context("@Context");

    private final String description;

    ParameterType(String value) {
        description = value;
    }

    public String getDescription() {
        return description;
    }
}
