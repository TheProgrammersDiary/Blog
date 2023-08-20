package com.evalvis.blog.comment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import protobufs.CommentRequest;

import java.util.List;

@RestController
@RequestMapping("comments")
final class CommentController {

    private final CommentRepository commentRepository;

    @Autowired
    CommentController(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @PostMapping(value = "/create")
    ResponseEntity<CommentRepository.CommentEntry> create(
            @RequestBody CommentRequest commentRequest
    )
    {
        return ResponseEntity.ok(
                new Comment(
                        commentRequest.getAuthor(), commentRequest.getContent(),
                        commentRequest.getPostId()
                ).create(commentRepository)
        );
    }

    @GetMapping(value = "/list-comments/{postId}")
    ResponseEntity<List<CommentRepository.CommentEntry>> listCommentsOfPost(
            @PathVariable String postId
    ) {
        return ResponseEntity.ok(commentRepository.findCommentEntriesByPostEntryId(postId));
    }
}
