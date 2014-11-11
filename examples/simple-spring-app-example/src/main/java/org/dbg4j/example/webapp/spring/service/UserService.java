package org.dbg4j.example.webapp.spring.service;

import java.util.Arrays;
import java.util.UUID;

import org.dbg4j.core.annotations.Debug;
import org.dbg4j.example.webapp.spring.domain.User;
import org.jfairy.Fairy;
import org.jfairy.producer.person.Person;
import org.springframework.stereotype.Component;

/**
 * Simple user service. Uses random names generator to crate user
 *
 * @author Maksym Fedoryshyn
 */
@Component
public class UserService {

    @Debug
    public User getCurrentUser(){
        User user = new User();
        Fairy fairy = Fairy.create();
        Person person = fairy.person();

        user.setId(UUID.randomUUID().toString());
        user.setFirstName(person.firstName());
        user.setLastName(person.lastName());
        user.setEmail(person.email());
        user.setRoles(Arrays.asList("ROLE_USER", "ROLE_ADMIN"));

        return user;
    }

    @Debug
    public User getUser(String userEmail){
        User user = new User();
        Fairy fairy = Fairy.create();
        Person person = fairy.person();

        user.setId(UUID.randomUUID().toString());
        user.setFirstName(person.firstName());
        user.setLastName(person.lastName());
        user.setEmail(userEmail);
        user.setRoles(Arrays.asList("ROLE_USER", "ROLE_ADMIN"));

        return user;
    }

}
