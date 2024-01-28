package com.evalvis.blog.user;

import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordChange {
    private final String currentPassword;
    private final String newPassword;

    public PasswordChange(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public void changePassword(UserRepository repository, String username, PasswordEncoder encoder) {
        if(repository.findPasswordByUsername(username).isEmpty()) {
            throw new RuntimeException("Can't change password when using OAuth!");
        }
        if(!encoder.matches(currentPassword, repository.findPasswordByUsername(username).get())) {
            throw new RuntimeException("Old and new passwords do not match!");
        }
        repository.save(
                UserRepository.UserEntry.withChangedPassword(
                        encoder.encode(newPassword), repository.findByUsername(username).get()
                )
        );
    }
}
