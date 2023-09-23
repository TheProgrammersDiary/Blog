package com.evalvis.blog.comment;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("mongoDbCommentRepository")
public interface CommentRepository
        extends MongoRepository<CommentRepository.CommentEntry, String> {

    List<CommentEntry> findCommentEntriesByPostEntryId(String postId);

    @Document("comment")
    @JsonPropertyOrder(alphabetic=true)
    class CommentEntry {

        @Id
        @Indexed(unique = true)
        private String id;
        private String author;
        private String content;
        private String postEntryId;

        public CommentEntry(String author, String content, String postEntryId) {
            this.author = author;
            this.content = content;
            this.postEntryId = postEntryId;
        }

        public CommentEntry() {
        }

        public String getId() {
            return id;
        }

        public String getAuthor() {
            return author;
        }

        public String getContent() {
            return content;
        }

        public String getPostEntryId() {
            return postEntryId;
        }
    }
}
