package com.evalvis.blog.user;

import com.evalvis.security.BlacklistedJwtTokenRepository;
import com.evalvis.security.JwtKey;
import com.evalvis.security.JwtToken;
import com.evalvis.security.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class OAuth2AuthorizationSuccessHandler implements AuthenticationSuccessHandler {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BlacklistedJwtTokenRepository blacklistedJwtTokenRepository;
    @Autowired
    private JwtKey key;

    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthorizationSuccessHandler.class);

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
        UserDetails userDetails = new User(username, null);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        JwtToken token = JwtToken.create(authToken, key.value(), blacklistedJwtTokenRepository);
        ResponseCookie cookie = ResponseCookie.from("jwt", token.value())
                .httpOnly(true)
                //.secure(true) // TODO: uncomment then HTTPS is enabled.
                .maxAge(Duration.ofMinutes(10))
                .path("/")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        response.sendRedirect(
                "https://localhost:3000/auth_login_success" +
                        "?username=" + URLEncoder.encode(token.username(), StandardCharsets.UTF_8) +
                        "&expirationDate="
                        + URLEncoder.encode(token.expirationDate().toString(), StandardCharsets.UTF_8)
        );
    }
}
