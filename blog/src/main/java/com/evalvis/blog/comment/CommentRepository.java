package com.evalvis.blog.comment;

import com.evalvis.blog.Repository;

import java.util.List;

public interface CommentRepository<T extends CommentRepository.CommentEntry>
        extends Repository<T, CommentRepository.CommentEntry> {

    List<CommentRepository.CommentEntry> findCommentEntriesByPostEntryId(String postId);

    interface CommentEntry extends Entry {
        String getAuthor();
        String getContent();
        String getPostEntryId();
    }
}
