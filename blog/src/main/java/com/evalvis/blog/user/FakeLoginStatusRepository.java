package com.evalvis.blog.user;

import java.util.*;
import java.util.stream.Collectors;

public class FakeLoginStatusRepository implements LoginStatusRepository {
    private final List<LoginStatusEntry> entries = new ArrayList<>();

    @Override
    public List<LoginStatusEntry> findLogoutCandidates(String email) {
        return entries.stream().filter(loginStatus -> loginStatus.getEmail().equals(email)).collect(Collectors.toList());
    }

    @Override
    public boolean notLoggedOutUserPresent(String email) {
        Date now = new Date();
        for (LoginStatusEntry entry : entries) {
            if (entry.getEmail().equals(email)
                    && entry.getLogoutDate() == null
                    && entry.getTokenExpirationDate().after(now)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public <S extends LoginStatusEntry> S save(S entry) {
        entries.add(entry);
        return entry;
    }

    @Override
    public <S extends LoginStatusEntry> Iterable<S> saveAll(Iterable<S> entries) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Optional<LoginStatusEntry> findById(String id) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public boolean existsById(String id) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Iterable<LoginStatusEntry> findAll() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Iterable<LoginStatusEntry> findAllById(Iterable<String> ids) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public long count() {
        return entries.size();
    }

    @Override
    public void deleteById(String id) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public void delete(LoginStatusEntry entry) {
        entries.remove(entry);
    }

    @Override
    public void deleteAllById(Iterable<? extends String> ids) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public void deleteAll(Iterable<? extends LoginStatusEntry> entries) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public void deleteAll() {
        entries.clear();
    }
}
