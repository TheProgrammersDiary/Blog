package com.evalvis.blog;

public interface Repository<T> {
    T save(T entity);

    interface Entry {
        String getId();
    }
}
