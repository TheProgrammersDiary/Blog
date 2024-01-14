package com.evalvis.blog.user;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.junit5.SnapshotExtension;
import com.evalvis.blog.FakeHttpServletRequest;
import com.evalvis.blog.FakeHttpServletResponse;
import com.evalvis.security.BlacklistedJwtTokenRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ITUserTestConfig.class)
@ActiveProfiles("it")
@ExtendWith({SnapshotExtension.class})
public class ITUserTests {
    private Expect expect;

    private final UserController controller;
    private final UserRepository repository;
    private final BlacklistedJwtTokenRepository blacklistedJwtTokenRepository;
    private final OAuth2AuthorizationSuccessHandler oAuth2AuthorizationSuccessHandler;
    private final UserMother mother;

    @Autowired
    public ITUserTests(
            UserController controller, UserRepository repository,
            BlacklistedJwtTokenRepository blacklistedJwtTokenRepository,
            OAuth2AuthorizationSuccessHandler oAuth2AuthorizationSuccessHandler
    ) {
        this.controller = controller;
        this.repository = repository;
        this.blacklistedJwtTokenRepository = blacklistedJwtTokenRepository;
        this.oAuth2AuthorizationSuccessHandler = oAuth2AuthorizationSuccessHandler;
        this.mother = new UserMother(this.controller);
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.getContext().setAuthentication(null);
        SecurityContextHolder.clearContext();
    }

    @Test
    void signsUp() {
        mother.signUp("tester");

        assertTrue(repository.findByUsername("tester").isPresent());
    }

    @Test
    void logsIn() {
        mother.loginNewUser();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
    }

    @Test
    void logsOut() {
        String jwt = mother
                .loginNewUser()
                .getHeader(HttpHeaders.SET_COOKIE)
                .split("jwt=")[1]
                .split(";")[0];

        controller.logout(new FakeHttpServletRequest(Map.of("Authorization", "Bearer " + jwt)));

        assertTrue(blacklistedJwtTokenRepository.isTokenBlacklisted(jwt));
    }

    @Test
    void logsInViaOAuth2() throws IOException {
        String username = UUID.randomUUID().toString();
        DefaultOidcUser user = new DefaultOidcUser(
                Collections.emptyList(),
                new OidcIdToken(
                        "fakeToken", Instant.now(), Instant.now().plus(Duration.ofMinutes(10)),
                        Map.of("email", UUID.randomUUID().toString(), "name", username, "sub", username)
                )
        );

        loginViaOauth2(user);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
    }

    @Test
    void logsInViaOAuth2MultipleTimes() throws IOException {
        String username = UUID.randomUUID().toString();
        DefaultOidcUser user = new DefaultOidcUser(
                Collections.emptyList(),
                new OidcIdToken(
                        "fakeToken", Instant.now(), Instant.now().plus(Duration.ofMinutes(10)),
                        Map.of("email", UUID.randomUUID().toString(), "name", username, "sub", username)
                )
        );

        loginViaOauth2(user);
        loginViaOauth2(user);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
    }

    private void loginViaOauth2(DefaultOidcUser user) throws IOException {
        oAuth2AuthorizationSuccessHandler.onAuthenticationSuccess(
                new FakeHttpServletRequest(),
                new FakeHttpServletResponse(),
                new OAuth2AuthenticationToken(user, user.getAuthorities(), "google")
        );
    }

    @Test
    void FailsToLoginViaOAuth2WithUnsupportedProvider() {
        String username = UUID.randomUUID().toString();
        DefaultOidcUser user = new DefaultOidcUser(
                Collections.emptyList(),
                new OidcIdToken(
                        "fakeToken", Instant.now(), Instant.now().plus(Duration.ofMinutes(10)),
                        Map.of("email", UUID.randomUUID().toString(), "name", username, "sub", username)
                )
        );

        assertThrows(
                RuntimeException.class,
                () -> oAuth2AuthorizationSuccessHandler.onAuthenticationSuccess(
                        new FakeHttpServletRequest(),
                        new FakeHttpServletResponse(),
                        new OAuth2AuthenticationToken(user, user.getAuthorities(), "fakeProvider")
                )
        );
    }

    @Test
    void failsToLoginNonExistingUser() {
        mother.signUp("tester1", "tester1@gmail.com", "test");

        assertThrows(
                BadCredentialsException.class,
                () -> mother.login("non-existing-user", "tester1@gmail.com", "test")
        );
    }

    @Test
    void failsToLoginWithWrongPassword() {
        mother.signUp("tester2", "tester2@gmail.com", "test");

        assertThrows(
                BadCredentialsException.class,
                () -> mother.login("tester2", "tester2@gmail.com", "wrong-password")
        );
    }

    @Test
    void throwsErrorIfUserCallsLogoutWithoutJwt() {
        assertThrows(RuntimeException.class, () -> controller.logout(new FakeHttpServletRequest(Map.of())));
    }
}
