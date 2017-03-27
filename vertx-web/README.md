# Vert.x Web Annotation
## Usage:

define a web service interface:

```java
@Path("/hello")
public static class HelloResource {

	@Get("/g")
	public void get(RoutingContext ctx) {
		ctx.response().end("Hello ws!");
	}

	@Post("/p")
	@Produces({ "text/plain" })
	@Consumes({ "text/json" })
	public void post(RoutingContext ctx) {
		ctx.request().bodyHandler(rs -> {
			String content = rs.toJsonObject().getString("content");
			ctx.response().end("Hello " + content + "!");
		});
	}
}
```

deployment web service like this:

```java
	Vertx vertx = Vertx.vertx();
	Router router = RouterBuilder.create(vertx).route(new HelloResource()).build();
	vertx.createHttpServer().requestHandler(router::accept).listen(8080);
	System.out.println("server work on 8080");
```

invoke web service like this:

```java
HttpClient httpClient = vertx.createHttpClient();
httpClient.post(8080, "localhost", "/hello/p").putHeader("Content-Type", "text/json")
	.handler(res -> {
		res.bodyHandler(buff -> {
			System.out.println(buff.toString());
		});
	}).end(new JsonObject().put("content", "ws").toString());
```



