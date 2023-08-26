package com.evalvis.blog;

public interface Repository<T extends Repository.Entry> {
    T save(T entity);

    interface Entry {
        String getId();
    }
}
