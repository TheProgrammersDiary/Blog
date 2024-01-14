package com.evalvis.blog.user;

import com.evalvis.blog.FakeHttpServletResponse;

import java.io.IOException;
import java.util.UUID;

public class UserMother {
    private final UserController controller;

    public UserMother(UserController controller) {
        this.controller = controller;
    }

    public FakeHttpServletResponse loginNewUser(String username, String email, String password) {
        signUp(username, email, password);
        return login(username, email, password);
    }

    public FakeHttpServletResponse loginNewUser() {
        String username = UUID.randomUUID().toString();
        signUp(username);
        return login(username, UUID.randomUUID().toString(), "test");
    }

    public FakeHttpServletResponse login(String username, String email, String password) {
        try {
            FakeHttpServletResponse response = new FakeHttpServletResponse();
            controller.login(new User(username, email, password), response);
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void signUp(String username, String email, String password) {
        controller.signUp(new User(username, email, password)).getBody();
    }

    public void signUp(String username) {
        controller.signUp(new User(username, "tester@gmail.com", "test")).getBody();
    }
}
