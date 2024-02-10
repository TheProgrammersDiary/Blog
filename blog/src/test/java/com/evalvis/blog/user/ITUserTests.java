package com.evalvis.blog.user;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.junit5.SnapshotExtension;
import com.evalvis.blog.FakeHttpServletRequest;
import com.evalvis.blog.FakeHttpServletResponse;
import com.evalvis.security.JwtKey;
import com.evalvis.security.JwtRefreshToken;
import com.evalvis.security.JwtShortLivedToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

@SpringBootTest(classes = ITUserTestsConfig.class)
@ActiveProfiles("it")
@ExtendWith({SnapshotExtension.class})
public class ITUserTests {
    private Expect expect;
    private final UserController controller;
    private final UserRepository userRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final FakeLoginStatusRepository loginStatusRepository;
    private final OAuth2AuthorizationSuccessHandler oAuth2AuthorizationSuccessHandler;
    private final JwtKey jwtKey;
    private final UserMother mother;

    @Autowired
    public ITUserTests(
            UserController controller, UserRepository userRepository, PasswordResetRepository passwordResetRepository,
            FakeLoginStatusRepository loginStatusRepository,
            OAuth2AuthorizationSuccessHandler oAuth2AuthorizationSuccessHandler, JwtKey jwtKey
    ) {
        this.controller = controller;
        this.userRepository = userRepository;
        this.passwordResetRepository = passwordResetRepository;
        this.loginStatusRepository = loginStatusRepository;
        this.oAuth2AuthorizationSuccessHandler = oAuth2AuthorizationSuccessHandler;
        this.jwtKey = jwtKey;
        this.mother = new UserMother(this.controller);
    }

    @AfterEach
    void cleanUp() {
        logout();
    }

    @Test
    void signsUp() {
        mother.signUp("tester@gmail.com", "tester", "test");

        assertTrue(userRepository.findByEmail("tester@gmail.com").isPresent());
    }

    @Test
    void logsIn() {
        mother.loginNewUser("ghi@gmail.com", "ghi", "password");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
        assertTrue(loginStatusRepository.notLoggedOutUserPresent("ghi@gmail.com"));
    }

    @Test
    void logsInViaOAuth2() throws IOException {
        loginViaOauth2("anotheremail@gmail.com");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
        assertTrue(loginStatusRepository.notLoggedOutUserPresent("anotheremail@gmail.com"));
    }

    @Test
    void logsInViaOAuth2MultipleTimes() throws IOException {
        String email = UUID.randomUUID().toString();

        loginViaOauth2(email);
        loginViaOauth2(email);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
    }

    @Test
    void refreshesToken() {
        JwtRefreshToken refreshToken = JwtRefreshToken.create(
                new FakeAuthentication("user@gmail.com", null), jwtKey.value()
        );

        JsonNode responseBody = assertDoesNotThrow(
                () -> new ObjectMapper().readTree(
                        controller.refreshJwt(
                        new FakeHttpServletRequest(
                                new Cookie[]{
                                        new Cookie("jwt", refreshToken.value())
                                }
                        )
                ).getBody()
                )
        );

        assertEquals("user@gmail.com", responseBody.get("username").asText());
        assertNotNull(responseBody.get("jwtShortLived").toString());
    }

    @Test
    void logsOut() {
        mother.loginNewUser("anotherTester@gmail.com", "t", "password");
        JwtRefreshToken refreshToken = JwtRefreshToken.create(
                new FakeAuthentication("anotherTester@gmail.com", null), jwtKey.value()
        );
        HttpServletRequest request = new FakeHttpServletRequest(
                Map.of(
                        "Authorization", "Bearer " + JwtShortLivedToken.create(refreshToken, jwtKey.value()).value()
                ),
                new Cookie[]{new Cookie("jwt", refreshToken.value())}
        );
        HttpServletResponse response = new FakeHttpServletResponse();

        controller.logout(request, response);

        assertTrue(response.getHeader("Set-Cookie").contains("jwt=; Path=/; Max-Age=0;"));
        assertFalse(loginStatusRepository.notLoggedOutUserPresent("anotherTester@gmail.com"));
    }

    @Test
    void changesPassword() {
        mother.loginNewUser("abc@gmail.com", "abc", "currentPassword");

        controller.changePassword(new PasswordChange("currentPassword", "newPassword"));

        logout();
        mother.login("abc@gmail.com", "newPassword");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
    }

    @Test
    void failsToChangePasswordWhenOldAndNewPasswordsMismatch() {
        mother.loginNewUser("abc@gmail.com", "abc", "currentPassword");

        assertThrows(
                RuntimeException.class,
                () -> controller.changePassword(
                        new PasswordChange("wrongPassword", "newPassword")
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
                        new PasswordChange(password, "newPassword")
                )
        );
    }

    @Test
    void resetsPassword() throws IOException {
        mother.loginNewUser("email@gmail.com", "testUser", "forgottenPassword");
        passwordResetRepository.save(
                new PasswordResetRepository.PasswordResetEntry(
                        new BCryptPasswordEncoder().encode("token"), "email@gmail.com"
                )
        );

        controller.resetPassword(new PasswordReset("email@gmail.com", "token", "newPassword"));

        controller.login(
                new LoginUser("email@gmail.com", "newPassword"),
                new FakeHttpServletResponse()
        );
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "wrongToken"})
    void failsToResetPasswordIfTokenIsInvalid(String wrongToken) throws IOException {
        mother.loginNewUser("testeremail@gmail.com", "tester", "forgottenPassword");
        passwordResetRepository.save(
                new PasswordResetRepository.PasswordResetEntry(
                        new BCryptPasswordEncoder().encode("token"), "testeremail@gmail.com"
                )
        );

        assertThrows(
                RuntimeException.class,
                () -> controller.resetPassword(
                        new PasswordReset("testeremail@gmail.com", wrongToken, "newPassword")
                )
        );
        SecurityContextHolder.getContext().setAuthentication(null);
        SecurityContextHolder.clearContext();
        assertThrows(
                BadCredentialsException.class,
                () -> controller.login(
                        new LoginUser("testeremail@gmail.com", "newPassword"),
                        new FakeHttpServletResponse()
                )
        );
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void requestsPasswordResetOnLocallyRegisteredUser() {
        mother.loginNewUser("localuser@gmail.com", "localUser", "notImportant");

        controller.requestPasswordReset("localuser@gmail.com");

        assertTrue(passwordResetRepository.existsByEmail("localuser@gmail.com"));
    }

    @Test
    void failsToRequestPasswordResetOnOauthUser() throws IOException {
        loginViaOauth2("oauthuser@gmail.com");

        controller.requestPasswordReset("oauthuser@gmail.com");

        assertFalse(passwordResetRepository.existsByEmail("oauthuser@gmail.com"));
    }

    private void loginViaOauth2(String email) throws IOException {
        DefaultOidcUser user = new DefaultOidcUser(
                Collections.emptyList(),
                new OidcIdToken(
                        "fakeToken", Instant.now(), Instant.now().plus(Duration.ofMinutes(10)),
                        Map.of("email", email, "name", UUID.randomUUID().toString(), "sub", email)
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
        String email = UUID.randomUUID().toString();
        DefaultOidcUser user = new DefaultOidcUser(
                Collections.emptyList(),
                new OidcIdToken(
                        "fakeToken", Instant.now(), Instant.now().plus(Duration.ofMinutes(10)),
                        Map.of("email", email, "name", UUID.randomUUID().toString(), "sub", email)
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
        mother.signUp("tester1@gmail.com", "tester1", "test");

        assertThrows(
                BadCredentialsException.class,
                () -> mother.login("nonexisting@gmail.com","test")
        );
    }

    @Test
    void failsToLoginWithWrongPassword() {
        mother.signUp("tester2@gmail.com", "tester2", "test");

        assertThrows(
                BadCredentialsException.class,
                () -> mother.login("tester2@gmail.com", "wrong-password")
        );
    }

    private void logout() {
        SecurityContextHolder.getContext().setAuthentication(null);
        SecurityContextHolder.clearContext();
    }
}