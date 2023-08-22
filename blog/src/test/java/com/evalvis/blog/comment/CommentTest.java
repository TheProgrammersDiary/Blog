package com.evalvis.blog.comment;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.annotations.SnapshotName;
import au.com.origin.snapshots.junit5.SnapshotExtension;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import protobufs.CommentRequest;

@ExtendWith(MockitoExtension.class)
@ExtendWith({SnapshotExtension.class})
public class CommentTest {

    private Expect expect;
    @Mock
    private CommentRepository<CommentRepository.CommentEntry> commentRepository;

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
                .build();

        CommentRepository.CommentEntry commentFromResponse = commentController.create(
                commentRequest
        ).getBody();

        Mockito.verify(commentRepository).save(Mockito.any(CommentRepository.CommentEntry.class));
        expect.toMatchSnapshot(new ObjectMapper().writeValueAsString(commentFromResponse));
    }
}
