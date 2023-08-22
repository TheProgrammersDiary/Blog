package com.evalvis.blog.post;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import protobufs.PostRequest;

@RestController
@RequestMapping("posts")
final class PostController {

    private final PostRepository springPostRepository;

    @Autowired
    PostController(PostRepository springPostRepository) {
        this.springPostRepository = springPostRepository;
    }

    @PostMapping(value = "/create")
    ResponseEntity<PostRepository.PostEntry> create(@RequestBody PostRequest postRequest)
    {
        return ResponseEntity.ok(
                new Post(
                postRequest.getAuthor(), postRequest.getTitle(), postRequest.getContent()
                ).create(springPostRepository)
        );
    }
}
