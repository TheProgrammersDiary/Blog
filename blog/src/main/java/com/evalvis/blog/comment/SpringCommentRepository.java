package com.evalvis.blog.comment;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import org.springframework.context.annotation.Primary;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("springCommentRepository")
@Primary
public interface SpringCommentRepository extends CommentRepository,
        CrudRepository<SpringCommentRepository.CommentEntry, String> {

    @Entity(name = "comment")
    @JsonPropertyOrder(alphabetic=true)
    class CommentEntry implements CommentRepository.CommentEntry {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        @Column(unique = true)
        private String id;
        @Column(nullable = false)
        private String author;
        @Column(nullable = false)
        private String content;
        @Column(nullable = false)
        private String postEntryId;

        CommentEntry(String author, String content, String postEntryId) {
            this.author = author;
            this.content = content;
            this.postEntryId = postEntryId;
        }

        public CommentEntry() {}

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getAuthor() {
            return author;
        }

        @Override
        public String getContent() {
            return content;
        }

        @Override
        public String getPostEntryId() {
            return postEntryId;
        }
    }
}
