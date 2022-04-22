# Vert.x Spring boot starter
## Usage:

create spring boot application like this:

```java
@SpringBootApplication
@EnableVertxWebServer
@Configuration
public class VertxStarterApplication {

    public static void main(String[] args) {
        SpringApplication.run(VertxStarterApplication.class, args);
    }

    @Bean
    @Lazy
    public Router webServerRouter(ApplicationContext applicationContext) {
        Router r = Router.router(applicationContext.getBean(Vertx.class));

        r.route("/health").handler(rc -> {
            rc.response().send("ok");
        });
        r.route().handler(StaticHandler.create().setCachingEnabled(false).setIndexPage("index.html"));

        return r;
    }
}

```

access rest service by browser: `http://localhost:8080/hello/g` 


