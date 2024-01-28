package com.evalvis.blog.user;

import com.evalvis.blog.FakeHttpServletResponse;

import java.io.IOException;
import java.util.UUID;

public class UserMother {
    private final UserController controller;

    public UserMother(UserController controller) {
        this.controller = controller;
    }

    public FakeHttpServletResponse loginNewUser(String email, String username, String password) {
        signUp(email, username, password);
        return login(email, password);
    }

    public FakeHttpServletResponse loginNewUser() {
        String email = UUID.randomUUID().toString();
        signUp(email, "tester", "test");
        return login(email, "test");
    }

    public FakeHttpServletResponse login(String email, String password) {
        try {
            FakeHttpServletResponse response = new FakeHttpServletResponse();
            controller.login(new LoginUser(email, password), response);
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void signUp(String email, String username, String password) {
        controller.signUp(new SignUpUser(email, username, password));
    }
}
