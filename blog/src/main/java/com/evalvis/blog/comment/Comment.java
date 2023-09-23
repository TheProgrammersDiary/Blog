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

    public CommentRepository.CommentEntry save(CommentRepository commentRepository) {
        return commentRepository.save(new CommentRepository.CommentEntry(author, content, postId));
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public String getPostId() {
        return postId;
    }
}
