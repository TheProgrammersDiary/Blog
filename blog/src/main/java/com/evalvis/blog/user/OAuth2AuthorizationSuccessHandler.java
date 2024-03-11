package com.evalvis.blog.user;

import com.evalvis.blog.Email;
import com.evalvis.blog.logging.BadRequestException;
import com.evalvis.security.JwtKey;
import com.evalvis.security.JwtRefreshToken;
import com.evalvis.security.JwtShortLivedToken;
import com.evalvis.security.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class OAuth2AuthorizationSuccessHandler implements AuthenticationSuccessHandler {
    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthorizationSuccessHandler.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtKey key;
    @Autowired
    private LoginStatusRepository loginStatusRepository;
    @Autowired
    private Email emailSender;
    @Autowired
    private PasswordEncoder encoder;
    @Value("${blog.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication
    ) throws IOException {
        log.info("Authentication: {}", authentication);
        String email;
        String username;
        String providerName = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        if(providerName.equals("google")) {
            email = ((DefaultOidcUser) authentication.getPrincipal()).getEmail();
            username = ((DefaultOidcUser) authentication.getPrincipal()).getAttribute("name");
        }
        else {
            throw new RuntimeException("Unrecognized Oauth2 provider.");
        }
        if (!userRepository.existsByEmail(email)) {
            userRepository.save(new UserRepository.UserEntry(username, email));
        }
        else if(userRepository.findPasswordByEmail(email).isPresent()) {
            throw new BadRequestException("User with email: " + email + " has signed up via different method.");
        }
        UserDetails userDetails = new User(email, null);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        JwtRefreshToken refreshToken = JwtRefreshToken.create(username, authToken, key.value(), false);
        new LoginUser(
                email, null
        ).login(
                loginStatusRepository, userRepository, emailSender, encoder, refreshToken.value(),
                refreshToken.expirationDate()
        );
        ResponseCookie jwtRefreshCookie = ResponseCookie
                .from("jwt", refreshToken.value())
                .httpOnly(true)
                .secure(true)
                .maxAge(Duration.ofDays(14))
                .path("/")
                .build();
        ResponseCookie jwtShortLivedCookie = ResponseCookie.from(
                        "jwtShortLived",
                        URLEncoder.encode(
                                JwtShortLivedToken.create(refreshToken, key.value()).value(), StandardCharsets.UTF_8
                        )
                )
                .httpOnly(false)
                .secure(true)
                .maxAge(Duration.ofMinutes(2))
                .path("/")
                .build();
        ResponseCookie usernameCookie = ResponseCookie.from(
                "username", URLEncoder.encode(username, StandardCharsets.UTF_8)
                )
                .httpOnly(false)
                .secure(true)
                .maxAge(Duration.ofMinutes(2))
                .path("/")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, jwtShortLivedCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, usernameCookie.toString());
        response.sendRedirect(frontendUrl + "/auth_login_success");
    }
}
