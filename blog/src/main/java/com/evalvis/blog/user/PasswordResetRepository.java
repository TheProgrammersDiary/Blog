package com.evalvis.blog.user;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface PasswordResetRepository extends CrudRepository<PasswordResetRepository.PasswordResetEntry, String> {
    Optional<PasswordResetEntry> findFirstByEmailOrderByDateCreatedDesc(String email);
    boolean existsByEmail(String email);
    @Entity(name = "password_reset")
    class PasswordResetEntry {
        @Id
        @Column(unique = true)
        public String resetToken;
        @Column(nullable = false)
        public String email;
        @CreationTimestamp
        @Temporal(TemporalType.TIMESTAMP)
        @Column(nullable = false)
        public Date dateCreated;

        public PasswordResetEntry(String resetToken, String email) {
            this.resetToken = resetToken;
            this.email = email;
            this.dateCreated = new Date();
        }

        public PasswordResetEntry() {
        }
    }
}
