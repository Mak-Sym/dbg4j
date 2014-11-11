package org.dbg4j.example.webapp.spring.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.dbg4j.core.annotations.Debug;
import org.dbg4j.example.webapp.spring.domain.Post;
import org.springframework.stereotype.Component;

/**
 * Simple posts service.
 *
 * @author Maksym Fedoryshyn
 */
@Component
public class UserPostsService {

    @Debug
    public List<Post> getPosts(String userId) {
        Random random = new Random(new Date().getTime());
        int numOfPosts = random.nextInt(10) + 1;
        List<Post> posts = new ArrayList<Post>(numOfPosts);

        for (int i = 0; i < numOfPosts; i++) {
            Post post = new Post();
            post.setId(Long.valueOf(random.nextInt(10000)));
            post.setSubject(this.generateRandomText(random.nextInt(5) + 5));
            post.setBody(this.generateRandomText(random.nextInt(20) + 20));
            posts.add(post);
        }
        return posts;
    }

    @Debug
    public Post getPost(long postId) {
        Random random = new Random(new Date().getTime());
        Post post = new Post();
        post.setId(postId);
        post.setSubject(this.generateRandomText(random.nextInt(5) + 5));
        post.setBody(this.generateRandomText(random.nextInt(20) + 20));
        return post;
    }

    @Debug
    private String generateRandomText(int nWords) {
        Random random = new Random(new Date().getTime());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nWords; i++) {
            sb.append(RandomStringUtils.randomAlphabetic(random.nextInt(8) + 1)).append(' ');
        }
        sb.append('.');
        return sb.toString();
    }
}
