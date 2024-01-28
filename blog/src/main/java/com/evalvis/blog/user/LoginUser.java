package com.evalvis.blog.user;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class LoginUser {
    private final String email;
    private final String password;

    public LoginUser(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public Authentication authenticate(AuthenticationManager authManager) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }

    public String getEmail() {
        return email;
    }


    public String getPassword() {
        return password;
    }

    public String username(UserRepository userRepository) {
        return userRepository.findByEmail(email).get().getUsername();
    }
}
