package com.evalvis.blog.post;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.annotations.SnapshotName;
import au.com.origin.snapshots.junit5.SnapshotExtension;

import com.evalvis.blog.Repository;
import com.evalvis.blog.comment.CommentRepository;
import com.evalvis.blog.comment.MongoDbCommentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import protobufs.CommentRequest;
import protobufs.PostRequest;

import java.util.Arrays;

import static shadow.org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith({SnapshotExtension.class})
@ActiveProfiles("ittest")
public class PostTest {

    private Expect expect;

    @Value(value="${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:15.4"
    );

    private static final MongoDBContainer mongoDB = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.data.mongodb.uri", mongoDB::getConnectionString);
    }

    @BeforeAll
    static void beforeAll() {
        postgres.start();
        mongoDB.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
        mongoDB.stop();
    }

    @Test
    public void check() {

    }

    @Test
    @SnapshotName("createsPostWithComments")
    public void listsPostComments() {
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
                    MongoDbCommentRepository.CommentEntry.class
            );
            commentIds[i] = createdComment.getId();
        }
        CommentRepository.CommentEntry[] commentsByPost = restTemplate.getForObject(
                "http://localhost:" + port + "/comments/list-comments/" + postFromResponse.getId(),
                MongoDbCommentRepository.CommentEntry[].class
        );

        assertThat(commentsByPost.length).isEqualTo(2);
        assertThat(
                Arrays.stream(commentsByPost).map(Repository.Entry::getId).toArray()
        ).isEqualTo(commentIds);
        expect.toMatchSnapshot(
                jsonWithMaskedProperties(commentsByPost, "id", "postEntryId")
        );
    }

    private <T> ArrayNode jsonWithMaskedProperties(
            T[] objects, String... properties
    ) {
        ArrayNode node = new ObjectMapper().valueToTree(objects);
        node.forEach(element ->
            Arrays
                    .stream(properties)
                    .forEach(property -> ((ObjectNode) element).put(property, "#hidden#"))
        );
        return node;
    }
}
