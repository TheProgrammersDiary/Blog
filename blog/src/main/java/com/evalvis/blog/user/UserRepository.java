package com.evalvis.blog.user;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<UserRepository.UserEntry, String> {
    @Query("SELECT user.password FROM blog_user user WHERE email = :email")
    Optional<String> findPasswordByEmail(String email);
    Optional<UserEntry> findByEmail(String email);
    boolean existsByEmail(String email);
    @Entity(name="blog_user")
    @JsonPropertyOrder(alphabetic=true)
    class UserEntry {
        @Id
        @Column(nullable = false, unique = true)
        @Email(message = "Invalid email address")
        private String email;
        @Column(nullable = false)
        private String username;
        private String password;

        public static UserEntry withChangedPassword(String newPassword, UserEntry userEntry) {
            userEntry.password = newPassword;
            return userEntry;
        }

        public UserEntry(String username, String email) {
            this(email, username, null);
        }

        public UserEntry(String email, String username, String password) {
            this.email = email;
            this.username = username;
            this.password = password;
        }

        public UserEntry() {
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserEntry userEntry = (UserEntry) o;
            return Objects.equals(email, userEntry.email);
        }

        @Override
        public int hashCode() {
            return Objects.hash(email);
        }
    }
}
