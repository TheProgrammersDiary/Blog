package com.evalvis.blog.user;

import com.evalvis.blog.FakeHttpServletResponse;

import java.io.IOException;

public class UserMother {
    private final UserController controller;
    private final UserRepository repo;

    public UserMother(UserController controller, UserRepository repo) {
        this.controller = controller;
        this.repo = repo;
    }

    public FakeHttpServletResponse loginNewUser(String email, String username, String password) {
        signUp(email, username, password);
        repo.save(UserRepository.UserEntry.withVerifiedToken(repo.findByEmail(email).get()));
        return login(email, password);
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
