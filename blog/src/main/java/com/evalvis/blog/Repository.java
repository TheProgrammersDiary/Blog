package com.evalvis.blog;

public interface Repository<T extends U, U extends Repository.Entry> {
    U save(T entity);

    interface Entry {
        String getId();
    }
}
