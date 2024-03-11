package com.evalvis.blog.user;

import com.evalvis.blog.Email;
import com.evalvis.blog.logging.BadRequestException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;

public class SignUpUser {
    private final String email;
    private final String username;
    private final String password;

    public SignUpUser(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public void save(
            UserRepository userRepository, PasswordEncoder encoder, Email emailSender, String verificationEndpoint
    ) {
        if(userRepository.findByEmail(email).isPresent()) {
            throw new BadRequestException("User with email: " + email + " already exists.");
        }
        String verificationToken = secureGuid().toString();
        String verificationPath = verificationEndpoint
                + "?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8)
                + "&verification-token=" + URLEncoder.encode(verificationToken, StandardCharsets.UTF_8);
        emailSender.sendEmail(
                email,
                "Email verification",
                "Hi. Thank you for creating a new account. Please verify your email address by going to link: "
                + "<a href=" + verificationPath + ">" + verificationPath + "</a>."
        );
        userRepository.save(new UserRepository.UserEntry(email, username, encoder.encode(password), verificationToken));
    }

    private static UUID secureGuid() {
        try {
            SecureRandom secureRandom = SecureRandom.getInstanceStrong();
            return new UUID(secureRandom.nextLong(), secureRandom.nextLong());
        } catch(NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate secure GUID.");
        }
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
