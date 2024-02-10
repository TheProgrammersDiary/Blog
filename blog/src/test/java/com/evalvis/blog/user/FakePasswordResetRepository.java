package com.evalvis.blog.user;

import java.util.*;
import java.util.stream.Collectors;

public class FakePasswordResetRepository implements PasswordResetRepository {
    private final Map<String, List<PasswordResetEntry>> entries = new HashMap<>();
    @Override
    public Optional<PasswordResetEntry> findFirstByEmailOrderByDateCreatedDesc(String email) {
        return entries
                .get(email)
                .stream()
                .max(Comparator.comparing(user -> user.getDateCreated()));
    }

    @Override
    public boolean existsByEmail(String email) {
        return entries.get(email) != null && entries.get(email).size() > 0;
    }

    @Override
    public <S extends PasswordResetEntry> S save(S entry) {
        entries.merge(entry.getEmail(), new ArrayList<>(List.of(entry)), (oldValue, newValue) -> {
            oldValue.addAll(newValue);
            return oldValue;
        });
        return entry;
    }

    @Override
    public <S extends PasswordResetEntry> Iterable<S> saveAll(Iterable<S> entities) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Optional<PasswordResetEntry> findById(String id) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public boolean existsById(String id) {
        return entries.get(id) != null;
    }

    @Override
    public Iterable<PasswordResetEntry> findAll() {
        return entries.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
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
