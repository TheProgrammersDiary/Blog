package com.evalvis.blog.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Repository
public interface LoginStatusRepository extends CrudRepository<LoginStatusRepository.LoginStatusEntry, String> {
    @Query("SELECT l FROM login_status l WHERE l.email = :email AND l.logoutDate IS NULL AND l.tokenExpirationDate > CURRENT_TIMESTAMP")
    List<LoginStatusEntry> findLogoutCandidates(String email);
    @Query("SELECT COUNT(u) > 0 FROM login_status u WHERE u.email = :email AND u.logoutDate IS NULL AND u.tokenExpirationDate > CURRENT_TIMESTAMP")
    boolean notLoggedOutUserPresent(String email);
    @Entity(name = "login_status")
    class LoginStatusEntry {
        @Id
        @Column
        @JsonIgnore
        private String token;
        @Column
        @JsonIgnore
        private String email;
        @CreationTimestamp
        @Temporal(TemporalType.TIMESTAMP)
        @Column(nullable = false)
        private Date loginDate;
        @Temporal(TemporalType.TIMESTAMP)
        @Column(nullable = false)
        private Date tokenExpirationDate;
        @Temporal(TemporalType.TIMESTAMP)
        @Column
        private Date logoutDate;

        public static LoginStatusEntry loggedOut(LoginStatusEntry entry) {
            entry.logoutDate = new Date();
            return entry;
        }

        public LoginStatusEntry(String token, String email, Date tokenExpirationDate) {
            this.token = token;
            this.email = email;
            this.loginDate = new Date();
            this.tokenExpirationDate = tokenExpirationDate;
        }

        public LoginStatusEntry() {
        }

        public String getToken() {
            return token;
        }

        public String getEmail() {
            return email;
        }

        public Date getTokenExpirationDate() {
            return tokenExpirationDate;
        }

        public Date getLogoutDate() {
            return logoutDate;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LoginStatusEntry that = (LoginStatusEntry) o;
            return token.equals(that.token);
        }

        @Override
        public int hashCode() {
            return Objects.hash(token);
        }
    }
}