# Vert.x Spring boot starter
## Usage:

define a rest service handler:

```java
import org.springframework.stereotype.Component;
import com.dinstone.vertx.web.annotation.Consumes;
import com.dinstone.vertx.web.annotation.Get;
import com.dinstone.vertx.web.annotation.Handler;
import com.dinstone.vertx.web.annotation.Path;
import com.dinstone.vertx.web.annotation.Post;
import com.dinstone.vertx.web.annotation.Produces;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

@Component
@Handler
@Path("/hello")
public class HelloHandler {

    @Get
    public void g(RoutingContext ctx) {
        ctx.response().end("Hello cloud!");
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

create spring boot application like this:

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.dinstone.vertx.starter.EnableVertxRest;

@SpringBootApplication
@EnableVertxRest
public class SpringCloudVertxApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudVertxApplication.class, args);
    }

}
```

invoke rest service like this:

```java
HttpClient httpClient = vertx.createHttpClient();
httpClient.post(8080, "localhost", "/api/hello/p").putHeader("Content-Type", "text/json")
	.handler(res -> {
		res.bodyHandler(buff -> {
			System.out.println(buff.toString());
		});
	}).end(new JsonObject().put("content", "ws").toString());
```

or access rest service by browser: `http://localhost:8080/hello/g` 


