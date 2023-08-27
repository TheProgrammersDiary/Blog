package com.evalvis.blog.post;

import com.evalvis.blog.Repository;

public interface PostRepository<T extends PostRepository.PostEntry>
        extends Repository<T, PostRepository.PostEntry> {

    interface PostEntry extends Entry {
        String getAuthor();
        String getTitle();
        String getContent();
    }
}
