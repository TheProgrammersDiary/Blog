package com.evalvis.blog.user;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.junit5.SnapshotExtension;
import com.evalvis.blog.FakeHttpServletRequest;
import com.evalvis.blog.FakeHttpServletResponse;
import com.evalvis.security.BlacklistedJwtTokenRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
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
        loginViaOauth2(UUID.randomUUID().toString());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
    }

    @Test
    void logsInViaOAuth2MultipleTimes() throws IOException {
        String username = UUID.randomUUID().toString();

        loginViaOauth2(username);
        loginViaOauth2(username);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
    }

    @Test
    void changesPassword() {
        String jwt = mother
                .loginNewUser("abc", "abc@gmail.com", "currentPassword")
                .getHeader(HttpHeaders.SET_COOKIE)
                .split("jwt=")[1]
                .split(";")[0];

        controller.changePassword(new UserPassword("currentPassword", "newPassword"));

        controller.logout(new FakeHttpServletRequest(Map.of("Authorization", "Bearer " + jwt)));
        mother.login("abc", "abc@gmail.com", "newPassword");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
    }

    @Test
    void failsToChangePasswordWhenOldAndNewPasswordsMismatch() {
        mother.loginNewUser("abc", "abc@gmail.com", "currentPassword");

        assertThrows(
                RuntimeException.class,
                () -> controller.changePassword(
                        new UserPassword("wrongPassword", "newPassword")
                )
        );
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "nonEmptyPassword"})
    void failsToChangePasswordWhenUsingOauth(String password) throws IOException {
        loginViaOauth2(UUID.randomUUID().toString());

        assertThrows(
                RuntimeException.class,
                () -> controller.changePassword(
                        new UserPassword(password, "newPassword")
                )
        );
    }

    private void loginViaOauth2(String username) throws IOException {
        DefaultOidcUser user = new DefaultOidcUser(
                Collections.emptyList(),
                new OidcIdToken(
                        "fakeToken", Instant.now(), Instant.now().plus(Duration.ofMinutes(10)),
                        Map.of("email", UUID.randomUUID().toString(), "name", username, "sub", username)
                )
        );
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
