package com.evalvis.blog.user;

import com.evalvis.blog.logging.BadRequestException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class SignUpUser {
    private final String email;
    private final String username;
    private final String password;

    public SignUpUser(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public void save(UserRepository userRepository, PasswordEncoder encoder) {
        if(userRepository.findByEmail(email).isPresent()) {
            throw new BadRequestException("User with email: " + email + " already exists.");
        }
        userRepository.save(new UserRepository.UserEntry(email, username, encoder.encode(password)));
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }


    public String getPassword() {
        return password;
    }
}
