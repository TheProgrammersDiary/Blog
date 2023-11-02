package com.evalvis.blog.user;

import org.springframework.security.crypto.password.PasswordEncoder;

public class User {
    private final String username;
    private final String email;
    private final String password;

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public void save(UserRepository userRepository, PasswordEncoder encoder) {
        userRepository.save(new UserRepository.UserEntry(username, email, encoder.encode(password)));
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
