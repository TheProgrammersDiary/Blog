package com.evalvis.blog.comment;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FakeCommentRepository implements CommentRepository {
    private final Map<String, CommentEntry> entries = new HashMap<>();

    @Override
    public List<CommentEntry> findCommentEntriesByPostEntryId(String postId) {
        return entries
                .values()
                .stream()
                .filter(comment -> comment.getPostEntryId().equals(postId)).collect(Collectors.toList());
    }

    @Override
    public <S extends CommentEntry> S insert(S entry) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public <S extends CommentEntry> List<S> insert(Iterable<S> entities) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public <S extends CommentEntry> Optional<S> findOne(Example<S> example) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public <S extends CommentEntry> List<S> findAll(Example<S> example) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public <S extends CommentEntry> List<S> findAll(Example<S> example, Sort sort) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public <S extends CommentEntry> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public <S extends CommentEntry> long count(Example<S> example) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public <S extends CommentEntry> boolean exists(Example<S> example) {
        return entries.values().stream().anyMatch(entry -> entry.equals(example.getProbe()));
    }

    @Override
    public <S extends CommentEntry, R> R findBy(
            Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction
    ) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public <S extends CommentEntry> S save(S entry) {
        entries.put(entry.getId(), entry);
        return entry;
    }

    @Override
    public <S extends CommentEntry> List<S> saveAll(Iterable<S> entities) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Optional<CommentEntry> findById(String id) {
        return Optional.ofNullable(entries.get(id));
    }

    @Override
    public boolean existsById(String id) {
        return entries.get(id) != null;
    }

    @Override
    public List<CommentEntry> findAll() {
        return entries.values().stream().toList();
    }

    @Override
    public List<CommentEntry> findAllById(Iterable<String> ids) {
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
    public void delete(CommentEntry entry) {
        entries.values().remove(entry);
    }

    @Override
    public void deleteAllById(Iterable<? extends String> ids) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public void deleteAll(Iterable<? extends CommentEntry> entries) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public void deleteAll() {
        entries.clear();
    }

    @Override
    public List<CommentEntry> findAll(Sort sort) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Page<CommentEntry> findAll(Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented.");
    }
}
