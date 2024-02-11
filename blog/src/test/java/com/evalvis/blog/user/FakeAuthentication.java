package com.evalvis.blog.user;

import com.evalvis.security.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public final class FakeAuthentication implements Authentication {
    private final com.evalvis.security.User user;
    private final String username;

    public FakeAuthentication(String email, String password, String username) {
        this.user = new User(email, password);
        this.username = username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Object getCredentials() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Object getDetails() {
        return username;
    }

    @Override
    public Object getPrincipal() {
        return user;
    }

    @Override
    public boolean isAuthenticated() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not implemented.");
    }
}
