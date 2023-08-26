package com.evalvis.blog.comment;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.annotations.SnapshotName;
import au.com.origin.snapshots.junit5.SnapshotExtension;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import protobufs.CommentRequest;

import java.util.Arrays;

@ExtendWith(MockitoExtension.class)
@ExtendWith({SnapshotExtension.class})
public class CommentTest {

    private Expect expect;
    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentController commentController;

    @Test
    @SnapshotName("createsComment")
    public void createsComment() throws JsonProcessingException {
        Mockito.when(
                commentRepository.save(Mockito.any(CommentRepository.CommentEntry.class))
        ).thenAnswer(mock -> mock.getArguments()[0]); // Return same object as was passed.
        CommentRequest commentRequest = CommentRequest
                .newBuilder()
                .setAuthor("AI")
                .setContent("Your post is better than I can write.")
                .setPostId("POST-ID")
                .build();

        CommentRepository.CommentEntry commentFromResponse = commentController.create(
                commentRequest
        ).getBody();

        Mockito.verify(commentRepository).save(Mockito.any(CommentRepository.CommentEntry.class));
        expect.toMatchSnapshot(jsonWithMaskedProperties(commentFromResponse, "id"));
    }

    private <T> ObjectNode jsonWithMaskedProperties(
            T object, String... properties
    ) throws JsonProcessingException {
        ObjectNode node = new ObjectMapper().valueToTree(object);
        Arrays.stream(properties).forEach(property -> node.put(property, "#hidden#"));
        return node;
    }
}
