package com.dinstone.vertx.rs.resolver;

import java.io.IOException;
import java.util.List;

import com.dinstone.vertx.rs.Assert;
import com.dinstone.vertx.rs.MessageConverters;
import com.dinstone.vertx.rs.MethodParameter;
import com.dinstone.vertx.rs.RouteDefinition;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;

public abstract class AbstractRouteResolver implements RouteResolver {

	private final static Logger LOG = LoggerFactory.getLogger(AbstractRouteResolver.class);

	@Override
	public void process(Router router, Object service, MessageConverters converters) {
		List<RouteDefinition> definitions = getDefinitions(service);
		for (RouteDefinition definition : definitions) {

			// bind method execution
			Route route;
			HttpMethod httpMethod = HttpMethod.valueOf(definition.getHttpMethod());
			if (definition.pathIsRegex()) {
				route = router.routeWithRegex(httpMethod, definition.getRoutePath());
			} else {
				route = router.route(httpMethod, definition.getRoutePath());
			}

			LOG.info("Registering route: {}", definition);

			// only register if request with body
			if (definition.hasBody() && definition.getConsumes() != null) {
				for (String item : definition.getConsumes()) {
					route.consumes(item); // ignore charset when binding
				}
			}

			if (definition.getProduces() != null) {
				for (String item : definition.getProduces()) {
					route.produces(item); // ignore charset when binding
				}
			}

			if (definition.getOrder() != 0) {
				route.order(definition.getOrder());
			}

			// add BodyHandler in case request has a body ...
			if (definition.hasBody()) {
				route.handler(BodyHandler.create());
			}

			// add CookieHandler in case cookies are expected
			if (definition.hasCookies()) {
				route.handler(CookieHandler.create());
			}

			// bind handler // blocking or async
			Handler<RoutingContext> handler;
			if (definition.isAsync()) {
				handler = asyncHandler(service, definition, converters);
			} else {
				handler = blockHandler(service, definition, converters);
			}

			route.handler(handler);
		}
	}

	protected abstract List<RouteDefinition> getDefinitions(Object service);

	private static Handler<RoutingContext> blockHandler(final Object toInvoke, final RouteDefinition definition,
			final MessageConverters converters) {

		return context -> context.vertx().executeBlocking(fut -> {
			try {
				Object[] args = prepareArguments(definition, context, converters);
				fut.complete(definition.getMethod().invoke(toInvoke, args));
			} catch (Throwable e) {
				fut.fail(e);
			}
		}, false, res -> {
			if (res.succeeded()) {
				try {
					Object result = res.result();
					produceResponse(result, context, definition, converters);
				} catch (Throwable e) {
					handleException(e, context, definition);
				}
			} else {
				handleException(res.cause(), context, definition);
			}
		});
	}

	private static Handler<RoutingContext> asyncHandler(final Object toInvoke, final RouteDefinition definition,
			MessageConverters converters) {

		return context -> {

			try {
				Object[] args = prepareArguments(definition, context, converters);
				Object result = definition.getMethod().invoke(toInvoke, args);

				if (result instanceof Future) {
					Future<?> fut = (Future<?>) result;

					// wait for future to complete ... don't block vertx event bus in the mean time
					fut.setHandler(handler -> {

						if (fut.succeeded()) {

							try {
								Object futureResult = fut.result();
								produceResponse(futureResult, context, definition, converters);
							} catch (Throwable e) {
								handleException(e, context, definition);
							}
						} else {
							handleException(fut.cause(), context, definition);
						}
					});
				}
			} catch (Throwable e) {
				handleException(e, context, definition);
			}
		};
	}

	private static Object[] prepareArguments(RouteDefinition definition, RoutingContext context,
			MessageConverters converters) throws Exception {
		List<MethodParameter> parameters = definition.getMethodParameters();
		Object[] arguments = new Object[parameters.size()];
		for (MethodParameter parameter : parameters) {
			switch (parameter.getParamType()) {
			case context:
				arguments[parameter.getParamIndex()] = getContextValue(definition, context, parameter);
				break;
			case cookie:
				Cookie cookie = context.getCookie(parameter.getParamName());
				arguments[parameter.getParamIndex()] = cookie == null ? null : cookie.getValue();
			case header:
				arguments[parameter.getParamIndex()] = context.request().getHeader(parameter.getParamName());
				break;
			case path:
				String pathParam = context.request().getParam(parameter.getParamName());
				arguments[parameter.getParamIndex()] = convertValue(parameter.getParamClazz(), pathParam);
				break;
			case query:
				String queryParam = context.request().getParam(parameter.getParamName());
				arguments[parameter.getParamIndex()] = convertValue(parameter.getParamClazz(), queryParam);
				break;
			case form:
				String formParam = context.request().getFormAttribute(parameter.getParamName());
				arguments[parameter.getParamIndex()] = convertValue(parameter.getParamClazz(), formParam);
				break;
			case matrix:
				String matrixParam = getMatrixParam(context.request(), parameter.getParamName());
				arguments[parameter.getParamIndex()] = convertValue(parameter.getParamClazz(), matrixParam);
				break;
			case body:
				arguments[parameter.getParamIndex()] = convertBean(definition, context, parameter, converters);
				break;
			default:
				break;
			}
		}
		return arguments;
	}

	private static String getMatrixParam(HttpServerRequest request, String paramName) {
		String[] items = request.uri().split(";");
		for (String item : items) {
			String[] nameValue = item.split("=");
			if (nameValue.length == 2 && nameValue[0].equals(paramName)) {
				return nameValue[1];
			}
		}

		return null;
	}

	private static Object convertValue(Class<?> paramClazz, String value) {
		if (value == null) {
			return null;
		}

		if (paramClazz.equals(String.class)) {
			return value;
		}

		// primitive types need to be cast differently
		if (paramClazz.isAssignableFrom(boolean.class) || paramClazz.isAssignableFrom(Boolean.class)) {
			return Boolean.valueOf(value);
		}

		if (paramClazz.isAssignableFrom(byte.class) || paramClazz.isAssignableFrom(Byte.class)) {
			return Byte.valueOf(value);
		}

		if (paramClazz.isAssignableFrom(char.class) || paramClazz.isAssignableFrom(Character.class)) {
			Assert.isTrue(value.length() != 0, "Expected Character but got: null");
			return value.charAt(0);
		}

		if (paramClazz.isAssignableFrom(short.class) || paramClazz.isAssignableFrom(Short.class)) {
			return Short.valueOf(value);
		}

		if (paramClazz.isAssignableFrom(int.class) || paramClazz.isAssignableFrom(Integer.class)) {
			return Integer.valueOf(value);
		}

		if (paramClazz.isAssignableFrom(long.class) || paramClazz.isAssignableFrom(Long.class)) {
			return Long.valueOf(value);
		}

		if (paramClazz.isAssignableFrom(float.class) || paramClazz.isAssignableFrom(Float.class)) {
			return Float.valueOf(value);
		}

		if (paramClazz.isAssignableFrom(double.class) || paramClazz.isAssignableFrom(Double.class)) {
			return Double.valueOf(value);
		}

		return null;
	}

	private static Object convertBean(RouteDefinition definition, RoutingContext context, MethodParameter parameter,
			MessageConverters converters) throws IOException {
		MessageConverter<Object> converter = converters.get(definition.getConsumes());
		if (converter != null) {
			return converter.read(parameter.getParamClazz(), context);
		}
		return null;
	}

	private static Object getContextValue(RouteDefinition definition, RoutingContext context,
			MethodParameter parameter) {
		Class<?> paramClazz = parameter.getParamClazz();
		// vert.x context
		if (paramClazz.isAssignableFrom(HttpServerResponse.class)) {
			return context.response();
		}

		if (paramClazz.isAssignableFrom(HttpServerRequest.class)) {
			return context.request();
		}

		if (paramClazz.isAssignableFrom(RoutingContext.class)) {
			return context;
		}

		// internal context / reflection of route definition
		if (paramClazz.isAssignableFrom(RouteDefinition.class)) {
			return definition;
		}

		// browse through context storage
		if (context.data() != null && context.data().size() > 0) {
			Object item = context.data().get(parameter.getParamName());
			if (item != null) {
				return item;
			}
		}

		throw new RuntimeException("Can't provide @Context of type: " + paramClazz);
	}

	private static <T> void produceResponse(Object result, RoutingContext context, RouteDefinition definition,
			MessageConverters converters) throws Throwable {

		HttpServerResponse response = context.response();
		// finish if not finished by writer
		// and is not an Async REST (Async RESTs must finish responses on their own)
		if (!definition.isAsync() && !response.ended()) {
			converters.get(definition.getProduces()).write(result, context);
			response.end();
		}
	}

	@SuppressWarnings("unchecked")
	private static void handleException(Throwable e, RoutingContext context, final RouteDefinition definition) {

		LOG.error("Handling exception: ", e);
		// ExecuteException ex = getExecuteException(e);
		//
		// // get appropriate exception handler/writer ...
		// ExceptionHandler handler;
		// try {
		// Class<? extends Throwable> clazz;
		// if (ex.getCause() == null) {
		// clazz = ex.getClass();
		// } else {
		// clazz = ex.getCause().getClass();
		// }
		//
		// Class<? extends ExceptionHandler>[] exHandlers = null;
		// if (definition != null) {
		// exHandlers = definition.getExceptionHandlers();
		// }
		//
		// handler = handlers.getExceptionHandler(clazz, exHandlers, injectionProvider,
		// context);
		// } catch (ClassFactoryException classException) {
		// // Can't provide exception handler ... rethrow
		// log.error("Can't provide exception handler!", classException);
		// // fall back to generic ...
		// handler = new GenericExceptionHandler();
		// ex = new ExecuteException(500, classException);
		// } catch (ContextException contextException) {
		// // Can't provide @Context for handler ... rethrow
		// log.error("Can't provide @Context!", contextException);
		// // fall back to generic ...
		// handler = new GenericExceptionHandler();
		// ex = new ExecuteException(500, contextException);
		// }

		HttpServerResponse response = context.response();
		response.setStatusCode(500);
		// handler.addResponseHeaders(definition, response);

		try {
			// handler.write(ex.getCause(), context.request(), context.response());

			// eventExecutor.triggerEvents(ex.getCause(), response.getStatusCode(),
			// definition, context,
			// injectionProvider);
		} catch (Throwable handlerException) {
			// this should not happen
			LOG.error("Failed to write out handled exception: " + e.getMessage(), e);
		}

		// end response ...
		if (!response.ended()) {
			response.end();
		}
	}

	public AbstractRouteResolver() {
		super();
	}

}