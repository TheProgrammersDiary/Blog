package com.evalvis.blog.user;

import com.evalvis.security.BlacklistedJwtTokenRepository;
import com.evalvis.security.JwtKey;
import com.evalvis.security.JwtToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Duration;

@RestController
@RequestMapping("users")
public class UserController {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final BlacklistedJwtTokenRepository blacklistedJwtTokenRepository;
    private final JwtKey key;

    public @Autowired UserController(
            UserRepository userRepository, PasswordEncoder encoder,
            AuthenticationManager authManager, BlacklistedJwtTokenRepository blacklistedJwtTokenRepository,
            JwtKey key
    ) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.authManager = authManager;
        this.blacklistedJwtTokenRepository = blacklistedJwtTokenRepository;
        this.key = key;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody User user) {
        user.save(userRepository, encoder);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public void login(@RequestBody User user, HttpServletResponse response) throws IOException {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        JwtToken token = JwtToken.create(authentication, key.value(), blacklistedJwtTokenRepository);
        ResponseCookie cookie = ResponseCookie.from("jwt", token.value())
                .httpOnly(true)
                //.secure(true) // TODO: uncomment then HTTPS is enabled.
                .maxAge(Duration.ofMinutes(10))
                .path("/")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        response.getWriter().println(
                new ObjectMapper()
                        .createObjectNode()
                        .put("username", token.username())
                        .put("expirationDate", token.expirationDate().toString())
                        .toPrettyString()
        );
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        JwtToken
                .existing(request, key.value(), blacklistedJwtTokenRepository)
                .ifPresentOrElse(
                        blacklistedJwtTokenRepository::blacklistToken,
                        () -> {
                            throw new RuntimeException("Possible security issue. Logout is missing jwt token.");
                        }
                );
    }

}
