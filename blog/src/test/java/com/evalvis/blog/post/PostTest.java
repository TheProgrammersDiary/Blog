package com.evalvis.blog.post;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.annotations.SnapshotName;
import au.com.origin.snapshots.junit5.SnapshotExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import protobufs.PostRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith({SnapshotExtension.class})
public class PostTest {

    private Expect expect;

    @Value(value="${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestPostRepository postRepository;

    @Test
    @SnapshotName("createsPost")
    public void createsPost() throws JsonProcessingException {
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

        assertThat(objectToString(postFromResponse)).isEqualTo(
                objectToString(postRepository.findFirstById(postFromResponse.getId()))
        );
        expect.toMatchSnapshot(jsonWithMaskedId(postFromResponse));
    }

    private <T> String objectToString(T object) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(object);
    }

    private <T> String jsonWithMaskedId(T object) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(
                (
                        (ObjectNode) new ObjectMapper().valueToTree(object)
                ).put("id", "#hidden#")
        );
    }
}
