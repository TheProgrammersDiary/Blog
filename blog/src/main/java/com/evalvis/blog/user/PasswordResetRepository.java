package com.evalvis.blog.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
        @JsonIgnore
        private String resetToken;
        @Column(nullable = false)
        @JsonIgnore
        private String email;
        @CreationTimestamp
        @Temporal(TemporalType.TIMESTAMP)
        @Column(nullable = false)
        private Date dateCreated;

        public PasswordResetEntry(String resetToken, String email) {
            this.resetToken = resetToken;
            this.email = email;
            this.dateCreated = new Date();
        }

        public PasswordResetEntry() {
        }

        public String getEmail() {
            return email;
        }

        public String getResetToken() {
            return resetToken;
        }

        public Date getDateCreated() {
            return dateCreated;
        }
    }
}
