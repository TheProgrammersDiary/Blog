package com.evalvis.blog.user;

import com.evalvis.blog.Email;
import com.evalvis.blog.logging.BadRequestException;
import com.evalvis.blog.logging.UnauthorizedException;
import com.evalvis.security.JwtKey;
import com.evalvis.security.JwtRefreshToken;
import com.evalvis.security.JwtShortLivedToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

@RestController
@RequestMapping("users")
public class UserController {
    private final UserRepository userRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final LoginStatusRepository loginStatusRepository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtKey key;
    private final Email emailSender;
    @Value("${blog.blog-url}")
    private String blogUrl;
    @Value("${blog.frontend-url}")
    private String frontendUrl;

    public @Autowired UserController(
            UserRepository userRepository, PasswordResetRepository passwordResetRepository,
            LoginStatusRepository loginStatusRepository, PasswordEncoder encoder, AuthenticationManager authManager,
            JwtKey key, Email emailSender
    ) {
        this.userRepository = userRepository;
        this.passwordResetRepository = passwordResetRepository;
        this.loginStatusRepository = loginStatusRepository;
        this.encoder = encoder;
        this.authManager = authManager;
        this.key = key;
        this.emailSender = emailSender;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody SignUpUser signUpUser) {
        signUpUser.save(userRepository, encoder, emailSender, blogUrl + "/verify-email");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/verify-email") // TODO: Consider if changing this to Post request is necessary (would probably need to modify FE).
    public void verifyEmail(
            @RequestParam(name = "email") String email,
            @RequestParam(name = "verification-token") String verificationToken,
            HttpServletResponse response
    ) throws IOException {
        Optional<UserRepository.UserEntry> user = userRepository.findByEmail(email);
        if(user.isEmpty() || !verificationToken.equals(user.get().getVerificationToken())) {
            throw new BadRequestException(
                    "Email " + email + " does not exists or verification token " + verificationToken + " does not match." +
                            " Or user with this email is already verified."
            );
        }
        userRepository.save(UserRepository.UserEntry.withVerifiedToken(user.get()));
        response.sendRedirect(frontendUrl);
    }

    @PostMapping("/login")
    void login(@RequestBody LoginUser loginUser, HttpServletResponse response) throws IOException {
        JwtRefreshToken refreshToken = loginUser.refreshToken(authManager, userRepository, key.value());
        loginUser.login(
                loginStatusRepository, userRepository, emailSender, encoder, refreshToken.value(),
                refreshToken.expirationDate()
        );
        ResponseCookie refreshCookie = ResponseCookie
                .from("jwt", refreshToken.value())
                .httpOnly(true)
                .secure(true)
                .maxAge(Duration.ofDays(14))
                .path("/")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        response.getWriter().println(
                new ObjectMapper()
                        .createObjectNode()
                        .put("username", loginUser.username(userRepository))
                        .put("jwtShortLived", JwtShortLivedToken.create(refreshToken, key.value()).value())
                        .toPrettyString()
        );
    }

    @PostMapping("/refresh")
    ResponseEntity<String> refreshJwt(HttpServletRequest request) {
        return JwtRefreshToken
                .existing(request, key.value())
                .map(refreshToken -> ResponseEntity.ok(
                        new ObjectMapper()
                            .createObjectNode()
                            .put("username", refreshToken.username())
                            .put("isLoginLocal", refreshToken.isLoginLocal())
                            .put("jwtShortLived", JwtShortLivedToken.create(refreshToken, key.value()).value())
                            .toPrettyString()
                        )
                )
                .orElseThrow(() -> new UnauthorizedException("Refresh token is not valid."));
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
    void logout(HttpServletRequest request, HttpServletResponse response) {
        ResponseCookie deleteCookie = ResponseCookie
                .from("jwt", "")
                .httpOnly(true)
                .secure(true)
                .maxAge(0)
                .path("/")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
        JwtRefreshToken refreshToken = JwtRefreshToken.existing(request, key.value()).get();
        for (LoginStatusRepository.LoginStatusEntry loginStatus : loginStatusRepository
                .findLogoutCandidates(refreshToken.email())) {
            if (encoder.matches(refreshToken.value(), loginStatus.getToken())) {
                loginStatusRepository.save(LoginStatusRepository.LoginStatusEntry.loggedOut(loginStatus));
                break;
            }
        }
    }
}
