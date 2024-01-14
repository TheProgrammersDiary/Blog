package com.evalvis.blog.comment;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.annotations.SnapshotName;
import au.com.origin.snapshots.junit5.SnapshotExtension;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ExtendWith({SnapshotExtension.class})
public class CommentTests {

    private Expect expect;

    private final CommentController controller;
    private final CommentMother mother;

    public CommentTests() {
        this.controller = new CommentController(new FakeCommentRepository());
        this.mother = new CommentMother(this.controller);
    }

    @Test
    void createsComment() {
        CommentRepository.CommentEntry comment = mother.create();

        expect.toMatchSnapshot(jsonWithMaskedProperties(comment, "id"));
    }

    private <T> ObjectNode jsonWithMaskedProperties(
            T object, String... properties
    ) {
        ObjectNode node = new ObjectMapper().valueToTree(object);
        Arrays.stream(properties).forEach(property -> node.put(property, "#hidden#"));
        return node;
    }

    @Test
    void findsPostComments() {
        List<CommentRepository.CommentEntry> savedComments = mother.createPostComments("postId");

        List<CommentRepository.CommentEntry> foundComments = new ArrayList(
                controller.listCommentsOfPost("postId").getBody()
        );

        Assertions.assertThat(foundComments)
                .extractingResultOf("toString")
                .containsExactlyInAnyOrderElementsOf(
                        savedComments.stream().map(CommentRepository.CommentEntry::toString).toList()
                );
    }
}
