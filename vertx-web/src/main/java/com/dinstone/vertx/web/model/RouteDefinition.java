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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * definition of a route
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

	public List<Argument> getArguments() {
		return arguments;
	}

	public void setArguments(List<Argument> arguments) {
		if (arguments != null) {
			this.arguments.addAll(arguments);
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

	public boolean pathIsRegex() {
		return routePath.indexOf("(") > 0 || routePath.indexOf("?<") > 0;
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

		return arguments.stream().anyMatch(arg -> ArgType.cookie.equals(arg.getArgType()));
	}

	@Override
	public String toString() {
		return "RouteDefinition [httpMethod=" + httpMethod + ", routePath=" + routePath + ", consumes="
				+ Arrays.toString(consumes) + ", produces=" + Arrays.toString(produces) + ", method=" + method
				+ ", arguments=" + arguments + "]";
	}

}
