package com.evalvis.blog.user;

import com.evalvis.blog.Email;
import com.evalvis.security.JwtRefreshToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.SecretKey;
import java.util.Date;

public class LoginUser {
    private final String email;
    private final String password;

    public LoginUser(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public void login(
            LoginStatusRepository loginStatusRepo, Email emailSender, PasswordEncoder encoder,
            String token, Date expirationDate
    ) {
        if(loginStatusRepo.notLoggedOutUserPresent(email)) {
            emailSender.sendEmail(
                    email,
                    "Duplicate login detected",
                    "Hi. Someone logged in to your account while another session has not yet ended." +
                            "If you did not recently login, your account might be breached. Contact us."
            );
        }
        loginStatusRepo.save(new LoginStatusRepository.LoginStatusEntry(encoder.encode(token), email, expirationDate));
    }

    public JwtRefreshToken refreshToken(AuthenticationManager authManager, UserRepository userRepo, SecretKey key) {
        Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return JwtRefreshToken.create(userRepo.findUsernameByEmail(email).get(), authentication, key, true);
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
