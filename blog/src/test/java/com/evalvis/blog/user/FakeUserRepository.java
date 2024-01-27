package com.evalvis.blog.user;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FakeUserRepository implements UserRepository {
    private final Map<String, UserEntry> entries = new HashMap<>();

    @Override
    public Optional<UserEntry> findByUsername(String username) {
        return entries.values().stream().filter(user -> user.getUsername().equals(username)).findFirst();
    }

    @Override
    public Optional<String> findPasswordByUsername(String username) {
        return entries
                .values()
                .stream()
                .filter(user -> user.getUsername().equals(username) && user.getPassword() != null)
                .map(UserEntry::getPassword)
                .findFirst();
    }

    @Override
    public boolean existsByEmail(String email) {
        return entries.values().stream().anyMatch(user -> user.getEmail().equals(email));
    }

    @Override
    public <S extends UserEntry> S save(S entry) {
        entries.put(entry.getId(), entry);
        return entry;
    }

    @Override
    public <S extends UserEntry> Iterable<S> saveAll(Iterable<S> entities) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Optional<UserEntry> findById(String id) {
        return Optional.ofNullable(entries.get(id));
    }

    @Override
    public boolean existsById(String id) {
        return entries.get(id) != null;
    }

    @Override
    public Iterable<UserEntry> findAll() {
        return entries.values();
    }

    @Override
    public Iterable<UserEntry> findAllById(Iterable<String> strings) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public long count() {
        return entries.size();
    }

    @Override
    public void deleteById(String id) {
        entries.remove(id);
    }

    @Override
    public void delete(UserEntry entry) {
        entries.values().remove(entry);
    }

    @Override
    public void deleteAllById(Iterable<? extends String> ids) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public void deleteAll(Iterable<? extends UserEntry> entries) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public void deleteAll() {
        entries.clear();
    }
}
