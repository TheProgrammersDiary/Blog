package com.evalvis.blog.user;

import com.evalvis.blog.logging.UnauthorizedException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordChange {
    private final String currentPassword;
    private final String newPassword;

    public PasswordChange(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public void changePassword(UserRepository repository, String email, PasswordEncoder encoder) {
        if(repository.findPasswordByEmail(email).isEmpty()) {
            throw new UnauthorizedException("Can't change password when using OAuth!");
        }
        if(!encoder.matches(currentPassword, repository.findPasswordByEmail(email).get())) {
            throw new UnauthorizedException("Old and new passwords do not match!");
        }
        repository.save(
                UserRepository.UserEntry.withChangedPassword(
                        encoder.encode(newPassword), repository.findByEmail(email).get()
                )
        );
    }
}
