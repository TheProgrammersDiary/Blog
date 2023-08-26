package com.evalvis.blog.comment;

import com.evalvis.blog.Repository;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

public interface CommentRepository extends Repository<CommentRepository.CommentEntry> {

    List<CommentRepository.CommentEntry> findCommentEntriesByPostEntryId(String postId);

    interface CommentEntry extends Entry {
        String getAuthor();
        String getContent();
        String getPostEntryId();
    }
}
