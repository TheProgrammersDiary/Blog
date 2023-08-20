package com.evalvis.blog.post;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.annotations.SnapshotName;
import au.com.origin.snapshots.junit5.SnapshotExtension;

import com.evalvis.blog.comment.CommentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import protobufs.CommentRequest;
import protobufs.PostRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith({SnapshotExtension.class})
public class PostTest {

    private Expect expect;

    @Value(value="${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @SnapshotName("createsPostWithComments")
    public void listsPostComments() throws JsonProcessingException {
        PostRequest postRequest = PostRequest
                .newBuilder()
                .setAuthor("Human")
                .setTitle("Testing matters")
                .setContent("You either test first, test along coding, or don't test at all.")
                .build();
        PostRepository.PostEntry postFromResponse = restTemplate.postForObject(
                "http://localhost:" + port + "/posts/create",
                postRequest, PostRepository.PostEntry.class
        );
        int commentCount = 2;
        for(int i = 0; i < commentCount; i++) {
            restTemplate.postForObject(
                    "http://localhost:" + port + "/comments/create",
                    CommentRequest
                            .newBuilder()
                            .setAuthor("author" + i)
                            .setContent("content" + i)
                            .setPostId(postFromResponse.getId())
                            .build(),
                    CommentRepository.CommentEntry.class
            );
        }

        CommentRepository.CommentEntry[] commentsFromResponse = restTemplate.getForObject(
                "http://localhost:" + port + "/comments/list-comments/" + postFromResponse.getId(),
                CommentRepository.CommentEntry[].class
        );

        expect.toMatchSnapshot(jsonWithMaskedId(commentsFromResponse));
    }

    private <T> String jsonWithMaskedId(T[] object) throws JsonProcessingException {
        ArrayNode node = new ObjectMapper().valueToTree(object);
        node.forEach(element -> ((ObjectNode) element).put("id", "#hidden#"));
        return new ObjectMapper().writeValueAsString(node);
    }
}
