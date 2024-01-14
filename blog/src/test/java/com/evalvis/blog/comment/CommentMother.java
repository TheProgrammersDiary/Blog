package com.evalvis.blog.comment;

import java.util.List;

public class CommentMother {
    private final CommentController controller;

    public CommentMother(CommentController controller) {
        this.controller = controller;
    }

    public CommentRepository.CommentEntry create() {
        return controller.create(new Comment("author", "content", "not-important")).getBody();
    }

    public List<CommentRepository.CommentEntry> createPostComments(String postId) {
        return List.of(
                controller.create(new Comment("author1", "content1", postId)).getBody(),
                controller.create(new Comment("author2", "content2", postId)).getBody()
        );
    }
}
