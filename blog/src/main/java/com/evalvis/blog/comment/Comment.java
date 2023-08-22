package com.evalvis.blog.comment;

public final class Comment {

    private final String author;
    private final String content;
    private final String postId;

    public Comment(String author, String content, String postId) {
        this.author = author;
        this.content = content;
        this.postId = postId;
    }

    public CommentRepository.CommentEntry create(
            CommentRepository<CommentRepository.CommentEntry> commentRepository
    ) {
        return commentRepository.save(
                new SpringCommentRepository.CommentEntry(author, content, postId)
        );
    }
}
