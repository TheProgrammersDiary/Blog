package com.evalvis.blog.user;

import com.evalvis.blog.Email;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Duration;

@RestController
@RequestMapping("users")
public class UserController {
    private final UserRepository userRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final BlacklistedJwtTokenRepository blacklistedJwtTokenRepository;
    private final JwtKey key;
    private final Email emailSender;

    public @Autowired UserController(
            UserRepository userRepository, PasswordResetRepository passwordResetRepository, PasswordEncoder encoder,
            AuthenticationManager authManager, BlacklistedJwtTokenRepository blacklistedJwtTokenRepository,
            JwtKey key, Email emailSender
    ) {
        this.userRepository = userRepository;
        this.passwordResetRepository = passwordResetRepository;
        this.encoder = encoder;
        this.authManager = authManager;
        this.blacklistedJwtTokenRepository = blacklistedJwtTokenRepository;
        this.key = key;
        this.emailSender = emailSender;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody SignUpUser signUpUser) {
        signUpUser.save(userRepository, encoder);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    void login(@RequestBody LoginUser loginUser, HttpServletResponse response) throws IOException {
        JwtToken token = JwtToken.create(
                loginUser.authenticate(authManager), key.value(), blacklistedJwtTokenRepository
        );
        ResponseCookie cookie = ResponseCookie.from("jwt", token.value())
                .httpOnly(true)
                .secure(true)
                .maxAge(Duration.ofMinutes(10))
                .path("/")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        response.getWriter().println(
                new ObjectMapper()
                        .createObjectNode()
                        .put("username", loginUser.username(userRepository))
                        .put("csrf", token.csrfToken())
                        .toPrettyString()
        );
    }

    @PatchMapping("/change-password")
    void changePassword(@RequestBody PasswordChange passwordChange) {
        passwordChange.changePassword(
                userRepository, SecurityContextHolder.getContext().getAuthentication().getName(), encoder
        );
    }

    @PostMapping("/request-password-reset")
    void requestPasswordReset(@RequestBody String email) {
        new PasswordResetRequest(email).request(passwordResetRepository, userRepository, emailSender, encoder);
    }

    @PatchMapping("/reset-password")
    ResponseEntity<String> resetPassword(@RequestBody PasswordReset passwordReset) {
        passwordReset.reset(passwordResetRepository, encoder, userRepository, emailSender);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    void logout(HttpServletRequest request) {
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
