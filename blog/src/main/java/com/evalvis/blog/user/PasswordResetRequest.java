package com.evalvis.blog.user;

import com.evalvis.blog.Email;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;

public class PasswordResetRequest {
    private final String email;

    public PasswordResetRequest(String email) {
        this.email = email;
    }

    public void request(
            PasswordResetRepository passwordResetRepository, UserRepository userRepository,
            Email emailSender, PasswordEncoder encoder
    ) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if(user.getPassword() == null) {
                return;
            }
            String resetToken = secureGuid().toString();
            passwordResetRepository.save(new PasswordResetRepository.PasswordResetEntry(encoder.encode(resetToken), email));
            emailSender.sendEmail(
                    email,
                    "Password reset request",
                    "Hi. You or somebody else has requested a password reset on your account. If it was not you"
                            + " no action is required. If it was you, please copy this token: " + resetToken +
                            " and paste it in the same page you requested your password."
            );
        });
    }

    private static UUID secureGuid() {
        try {
            SecureRandom secureRandom = SecureRandom.getInstanceStrong();
            return new UUID(secureRandom.nextLong(), secureRandom.nextLong());
        } catch(NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate secure GUID.");
        }
    }
}
