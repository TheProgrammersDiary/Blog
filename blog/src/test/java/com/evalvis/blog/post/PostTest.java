package com.evalvis.blog.post;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.annotations.SnapshotName;
import au.com.origin.snapshots.junit5.SnapshotExtension;

import com.evalvis.blog.Repository;
import com.evalvis.blog.comment.CommentRepository;
import com.evalvis.blog.comment.FileCommentRepository;
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

import java.util.Arrays;
import java.util.stream.Collectors;

import static shadow.org.assertj.core.api.AssertionsForClassTypes.assertThat;

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
                postRequest, SpringPostRepository.PostEntry.class
        );
        int commentCount = 2;
        String[] commentIds = new String[commentCount];

        for(int i = 0; i < commentCount; i++) {
            CommentRepository.CommentEntry createdComment = restTemplate.postForObject(
                    "http://localhost:" + port + "/comments/create",
                    CommentRequest
                            .newBuilder()
                            .setAuthor("author" + i)
                            .setContent("content" + i)
                            .setPostId(postFromResponse.getId())
                            .build(),
                    FileCommentRepository.CommentEntry.class
            );
            commentIds[i] = createdComment.getId();
        }
        CommentRepository.CommentEntry[] commentsByPost = restTemplate.getForObject(
                "http://localhost:" + port + "/comments/list-comments/" + postFromResponse.getId(),
                FileCommentRepository.CommentEntry[].class
        );

        assertThat(commentsByPost.length).isEqualTo(2);
        assertThat(
                Arrays.stream(commentsByPost).map(Repository.Entry::getId).toArray()
        ).isEqualTo(commentIds);
        expect.toMatchSnapshot(
                jsonWithMaskedProperties(commentsByPost, "id", "postEntryId")
        );
    }

    private <T> String jsonWithMaskedProperties(
            T[] objects, String... properties
    ) throws JsonProcessingException {
        ArrayNode node = new ObjectMapper().valueToTree(objects);
        node.forEach(element ->
            Arrays
                    .stream(properties)
                    .forEach(property -> ((ObjectNode) element).put(property, "#hidden#"))
        );
        return new ObjectMapper().writeValueAsString(node);
    }
}
