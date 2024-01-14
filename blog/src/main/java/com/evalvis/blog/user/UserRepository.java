package com.evalvis.blog.user;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends CrudRepository<UserRepository.UserEntry, String> {
    Optional<UserEntry> findByUsername(String username);
    boolean existsByEmail(String email);
    @Entity(name="blog_user")
    @JsonPropertyOrder(alphabetic=true)
    class UserEntry {
        @Id
        @Column(unique = true)
        private String id;
        @Column(nullable = false, unique = true)
        private String username;
        @Column(nullable = false, unique = true)
        @Email(message = "Invalid email address")
        private String email;
        private String password;

        public UserEntry(String username, String email) {
            this(username, email, null);
        }

        public UserEntry(String username, String email, String password) {
            this.id = UUID.randomUUID().toString();
            this.username = username;
            this.email = email;
            this.password = password;
        }

        public UserEntry() {
        }

        public String getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserEntry userEntry = (UserEntry) o;
            return Objects.equals(id, userEntry.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
}
