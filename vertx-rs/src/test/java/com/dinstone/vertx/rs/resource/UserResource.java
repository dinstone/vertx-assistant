package com.dinstone.vertx.rs.resource;

import com.dinstone.vertx.rs.annotation.FormParam;
import com.dinstone.vertx.rs.annotation.PathParam;
import com.dinstone.vertx.rs.annotation.Post;
import com.dinstone.vertx.rs.annotation.Produces;
import com.dinstone.vertx.rs.annotation.QueryParam;
import com.dinstone.vertx.rs.annotation.RestService;

@RestService("/ur")
@Produces("application/json")
public class UserResource {

    @Post("/c/:name")
    public UserBean c(@PathParam("name") String name, @QueryParam("age") int age, @FormParam("sex") boolean sex) {
        return new UserBean(name, age, sex);
    }
}
