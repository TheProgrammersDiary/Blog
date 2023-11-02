package com.evalvis.blog.user;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<UserRepository.UserEntry, String> {
    @Entity(name="blog_user")
    @JsonPropertyOrder(alphabetic=true)
    class UserEntry {
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        @Column(unique = true)
        private String id;
        @Column(nullable = false, unique = true)
        private String username;
        @Column(nullable = false, unique = true)
        @Email(message = "Invalid email address")
        private String email;
        @Column(nullable = false)
        private String password;

        public UserEntry(String username, String email, String password) {
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
    }
}
