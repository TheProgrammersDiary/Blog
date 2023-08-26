package com.evalvis.blog.post;

import com.evalvis.blog.Repository;

public interface PostRepository extends Repository<PostRepository.PostEntry> {

    interface PostEntry extends Entry {
        String getAuthor();
        String getTitle();
        String getContent();
    }
}
