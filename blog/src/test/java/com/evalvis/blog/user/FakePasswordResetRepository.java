package com.evalvis.blog.user;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FakePasswordResetRepository implements PasswordResetRepository {
    private final Map<String, PasswordResetEntry> entries = new HashMap<>();
    @Override
    public Optional<PasswordResetEntry> findFirstByEmailOrderByDateCreatedDesc(String email) {
        return entries.values()
                .stream()
                .filter(user -> user.email.equals(email))
                .max(Comparator.comparing(user -> user.dateCreated));
    }

    @Override
    public boolean existsByEmail(String email) {
        return entries.values().stream().anyMatch(user -> user.email.equals(email));
    }

    @Override
    public <S extends PasswordResetEntry> S save(S entry) {
        entries.put(entry.email, entry);
        return entry;
    }

    @Override
    public <S extends PasswordResetEntry> Iterable<S> saveAll(Iterable<S> entities) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Optional<PasswordResetEntry> findById(String id) {
        return Optional.ofNullable(entries.get(id));
    }

    @Override
    public boolean existsById(String id) {
        return entries.get(id) != null;
    }

    @Override
    public Iterable<PasswordResetEntry> findAll() {
        return entries.values();
    }

    @Override
    public Iterable<PasswordResetEntry> findAllById(Iterable<String> strings) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public long count() {
        return entries.size();
    }

    @Override
    public void deleteById(String entry) {
        entries.values().remove(entry);
    }

    @Override
    public void delete(PasswordResetEntry entry) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public void deleteAllById(Iterable<? extends String> ids) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public void deleteAll(Iterable<? extends PasswordResetEntry> entries) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public void deleteAll() {
        entries.clear();
    }
}
