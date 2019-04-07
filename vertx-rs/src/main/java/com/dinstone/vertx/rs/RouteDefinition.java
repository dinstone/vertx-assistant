package com.dinstone.vertx.rs;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import io.vertx.core.Future;

/**
 * Holds definition of a route as defined with annotations
 */
public class RouteDefinition {

	private final static String DELIMITER = "/";

	/**
	 * Path part given on class
	 */
	private String servicePath = null;

	/**
	 * Path part given on method
	 */
	private String methodPath = null;

	/**
	 * Converted path (the route), in case of regular expression paths otherwise
	 * null
	 */
	private String routePath = null;

	private String[] consumes = null;

	private String[] produces = null;

	private String httpMethod;

	private Method method;

	/**
	 * Route order lower is earlier or 0 for default
	 */
	private int order;

	/**
	 * Async response (don't close the writer)
	 */
	private boolean async;

	/**
	 * Type of return value ...
	 */
	private Class<?> returnType;

	private List<MethodParameter> methodParameters = new LinkedList<>();

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
		this.produces = produces;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	/**
	 * defines if execute blocking or async
	 *
	 * @return true async, false blocking
	 */
	public boolean isAsync() {
		return returnType.equals(Future.class) || returnType.isInstance(Future.class)
				|| Future.class.isAssignableFrom(returnType);
	}

	public void setReturnType(Class<?> returnType) {
		this.returnType = returnType;
	}

	public List<MethodParameter> getMethodParameters() {
		return methodParameters;
	}

	public void setMethodParameters(List<MethodParameter> methodParameters) {
		if (methodParameters != null) {
			this.methodParameters.addAll(methodParameters);
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

	public int getOrder() {
		return order;
	}

	public Class<?> getReturnType() {
		return returnType;
	}

	@Override
	public String toString() {
		return "RouteDefinition [httpMethod=" + httpMethod + ", routePath=" + routePath + ", consumes="
				+ Arrays.toString(consumes) + ", produces=" + Arrays.toString(produces) + ", method=" + method
				+ ", order=" + order + ", async=" + async + ", methodParameters=" + methodParameters + "]";
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
		if (methodParameters.size() == 0) {
			return false;
		}

		return methodParameters.stream().anyMatch(param -> ParamType.cookie.equals(param.getParamType()));
	}

}
