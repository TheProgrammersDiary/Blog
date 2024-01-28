package com.evalvis.blog.comment;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
            id = UUID.randomUUID().toString();
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CommentEntry that = (CommentEntry) o;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return "CommentEntry{" +
                    "id='" + id + '\'' +
                    ", author='" + author + '\'' +
                    ", content='" + content + '\'' +
                    ", postEntryId='" + postEntryId + '\'' +
                    '}';
        }
    }
}
