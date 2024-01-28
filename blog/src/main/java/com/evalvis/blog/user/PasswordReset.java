package com.evalvis.blog.user;

import com.evalvis.blog.Email;
import com.evalvis.blog.logging.UnauthorizedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;

public class PasswordReset {
    private final String email;
    private final String resetToken;
    private final String newPassword;

    public PasswordReset(String email, String resetToken, String newPassword) {
        this.email = email;
        this.resetToken = resetToken;
        this.newPassword = newPassword;
    }

    public void reset(
            PasswordResetRepository passwordResetRepository, PasswordEncoder encoder, UserRepository userRepository,
            Email emailSender
    ) {
        passwordResetRepository
                .findFirstByEmailOrderByDateCreatedDesc(email)
                .ifPresent(passwordChangeEntry -> {
                    if(
                            !encoder.matches(resetToken, passwordChangeEntry.resetToken)
                                    || new Date()
                                    .after(new Date(passwordChangeEntry.dateCreated.getTime() + 60 * 60 * 1000)
                            )
                    ) {
                        throw new UnauthorizedException("Reset token is incorrect or has already expired.");
                    }
                    emailSender.sendEmail(
                            email,
                            "Your password was reset",
                            "Hi. Your password was reset. If you did this, no action is required." +
                                    "If someone else did this your account is breached, please contact us immediately."
                    );
                    userRepository.save(
                            UserRepository.UserEntry.withChangedPassword(
                                    encoder.encode(newPassword), userRepository.findByEmail(email).get()
                            )
                    );
                });
    }
}
