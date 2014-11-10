package org.dbg4j.example.webapp.spring.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.dbg4j.example.webapp.spring.domain.Post;
import org.dbg4j.example.webapp.spring.domain.User;
import org.dbg4j.example.webapp.spring.service.UserPostsService;
import org.dbg4j.example.webapp.spring.service.UserService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Very simple controller. Handles requests to the home page (with user's name and random list of user's posts), post
 * page (detailed information about particular post) and list of user's posts in JSON format (just to show example
 * hoe dbg4j handles that type of responses)
 *
 * @author Maksym Fedoryshyn
 */
@Controller
public class HomeController {

    @Autowired
    UserService userService;

    @Autowired
    UserPostsService postsService;

    protected static final ObjectMapper mapper = new ObjectMapper(){
        {
            configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
    };

    @RequestMapping(method = RequestMethod.GET, value = {"/home", "/"})
    public String get(ModelMap model) {
        User user = userService.getCurrentUser();
        List<Post> userPosts = postsService.getPosts(user.getId());

        model.put("user", user);
        model.put("posts", userPosts);

        return "home";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/post")
    public String getPost(@RequestParam("postId") long postId, ModelMap model) {
        Post post = postsService.getPost(postId);

        model.put("post", post);

        return "post";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/posts.json")
    public void getPostsJson(@RequestParam("userEmail") String userEmail, HttpServletResponse response) throws
            Exception {
        String jsonStr = "{}";

        List<Post> posts = postsService.getPosts(userService.getUser(userEmail).getId());

        if (posts != null && posts.size() > 0) {
            JSONArray array = new JSONArray(mapper.writeValueAsString(posts));
            JSONObject json = new JSONObject();
            json.put("posts", array);
            jsonStr = json.toString();
        }

        response.setContentType(MediaType.APPLICATION_JSON);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonStr);
        response.setStatus(Response.Status.OK.getStatusCode());
        response.setHeader("Connection", "close");
    }

}
