package com.evalvis.blog.comment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("comments")
@CrossOrigin(origins = "http://localhost:3000")
final class CommentController {

    private final CommentRepository commentRepository;

    @Autowired
    CommentController(@Qualifier("mongoDbCommentRepository") CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @GetMapping(value = "/list-comments/{postId}")
    ResponseEntity<List<CommentRepository.CommentEntry>> listCommentsOfPost(
            @PathVariable String postId
    ) {
        return ResponseEntity.ok(commentRepository.findCommentEntriesByPostEntryId(postId));
    }

    @PostMapping(value = "/create")
    ResponseEntity<CommentRepository.CommentEntry> create(@RequestBody Comment comment)
    {
        return ResponseEntity.ok(comment.save(commentRepository));
    }
}
