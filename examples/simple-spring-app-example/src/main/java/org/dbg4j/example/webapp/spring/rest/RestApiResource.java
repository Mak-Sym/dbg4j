package org.dbg4j.example.webapp.spring.rest;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.dbg4j.core.annotations.Debug;
import org.dbg4j.example.webapp.spring.domain.Post;
import org.dbg4j.example.webapp.spring.domain.User;
import org.dbg4j.example.webapp.spring.service.UserPostsService;
import org.dbg4j.example.webapp.spring.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sun.jersey.api.NotFoundException;

/**
 * Simple rest service.
 *
 * @author Maksym Fedoryshyn
 */
@Component
@Scope("request")
@Path("/v1")
public class RestApiResource {
    @Autowired
    UserService userService;

    @Autowired
    UserPostsService userPostsService;

    @Context
    @Debug
    private UriInfo uriInfo;

    protected static final ObjectMapper mapper = new ObjectMapper(){
        {
            configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
    };

    @GET
    @Path("/posts/{userEmail}")
    @Produces("application/json")
    @Debug
    public String get(@PathParam("userEmail") String userEmail) throws IOException {
        List<Post> posts = userPostsService.getPosts(userService.getUser(userEmail).getId());

        if (posts == null || posts.size() == 0) {
            throw new NotFoundException(uriInfo.getRequestUri());
        }

        return mapper.writeValueAsString(posts);
    }

    @GET
    @Path("/users/{userEmail}")
    @Produces("application/json")
    @Debug
    public User getUser(@PathParam("userEmail") String userEmail) throws IOException {
        User user = userService.getUser(userEmail);
        if (user == null) {
            throw new NotFoundException(uriInfo.getRequestUri());
        }
        return user;
    }
}
