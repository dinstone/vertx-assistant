package com.dinstone.vertx.rs.model;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Holds definition of a route as defined with annotations
 */
public class RouteDefinition {

    private final static String DELIMITER = "/";

    /**
     * Path part given on class
     */
    private String servicePath;

    /**
     * Path part given on method
     */
    private String methodPath;

    /**
     * Route path for the route
     */
    private String routePath;

    private String[] consumes;

    private String[] produces;

    private String httpMethod;

    private Method method;

    private boolean futureType;

    /**
     * Type of return value
     */
    private Class<?> returnType;

    private List<Argument> arguments = new LinkedList<>();

    public RouteDefinition(String servicePath, String[] produces, String[] consumes, Method method) {
        this.servicePath = servicePath;
        this.produces = produces;
        this.consumes = consumes;
        this.method = method;
    }

    public String getServicePath() {
        return servicePath;
    }

    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }

    public String getMethodPath() {
        return methodPath;
    }

    public void setMethodPath(String methodPath) {
        this.methodPath = methodPath;
        if (!methodPath.startsWith(DELIMITER)) {
            routePath = servicePath + DELIMITER + methodPath;
        } else {
            routePath = servicePath + methodPath;
        }
    }

    public void setConsumes(String[] consumes) {
        if (consumes != null) {
            this.consumes = consumes;
        }
    }

    public void setProduces(String[] produces) {
        if (produces != null) {
            this.produces = produces;
        }
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }

    public List<Argument> getMethodParameters() {
        return arguments;
    }

    public void setMethodParameters(List<Argument> methodParameters) {
        if (methodParameters != null) {
            this.arguments.addAll(methodParameters);
        }
    }

    public String getRoutePath() {
        return routePath;
    }

    public String[] getConsumes() {
        return consumes;
    }

    public String[] getProduces() {
        return produces;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public boolean isFutureType() {
        return futureType;
    }

    public void setFutureType(boolean futureType) {
        this.futureType = futureType;
    }

    public boolean pathIsRegex() {
        return false;
    }

    public boolean hasBody() {
        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/
        // also see:
        // https://www.owasp.org/index.php/Test_HTTP_Methods_(OTG-CONFIG-006)
        return "DELETE".equals(httpMethod) || "POST".equals(httpMethod) || "PUT".equals(httpMethod)
                || "PATCH".equals(httpMethod) || "TRACE".equals(httpMethod);
    }

    public boolean hasCookies() {
        if (arguments.size() == 0) {
            return false;
        }

        return arguments.stream().anyMatch(param -> ParamType.cookie.equals(param.getParamType()));
    }

    @Override
    public String toString() {
        return "RouteDefinition [httpMethod=" + httpMethod + ", routePath=" + routePath + ", consumes="
                + Arrays.toString(consumes) + ", produces=" + Arrays.toString(produces) + ", method=" + method
                + ", futureType=" + futureType + ", arguments=" + arguments + "]";
    }

}
