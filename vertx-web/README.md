# Vert.x Web Annotation
## Usage:

define a web service interface:

```java

@WebHandler("/hello")
public static class HelloResource {

    @Get
    public void g(RoutingContext ctx) {
    		ctx.response().end("Hello ws!");
    }
    
    @Get("/g/:name")
    public void hello(RoutingContext ctx) {
        String message = "hello";
        String name = ctx.request().getParam("name");
        if (name != null) {
            message += " " + name;
        }

        JsonObject json = new JsonObject().put("message", message);
        ctx.response().end(json.encode());
    }
    
    @Post("/p")
    @Produces("text/plain")
    @Consumes("text/json")
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
RouterBuilder builder = RouterBuilder.create(vertx);
Router apiRouter = builder.handler(new HelloResource()).build();

Router router = Router.router(vertx).mountSubRouter("/api", apiRouter);
vertx.createHttpServer().requestHandler(router::accept).listen(8080);
System.out.println("server work on 8080");
System.out.println("access url: http://localhost:8080/api/hello/g");

```

invoke web service like this:

```java

HttpClient httpClient = vertx.createHttpClient();
httpClient.post(8080, "localhost", "/api/hello/p").putHeader("Content-Type", "text/json")
	.handler(res -> {
		res.bodyHandler(buff -> {
			System.out.println(buff.toString());
		});
	}).end(new JsonObject().put("content", "ws").toString());
	
```



