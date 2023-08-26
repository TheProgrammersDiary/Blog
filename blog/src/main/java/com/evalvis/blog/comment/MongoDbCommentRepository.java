package com.evalvis.blog.comment;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository("mongoDbCommentRepository")
public interface MongoDbCommentRepository extends CommentRepository,
        MongoRepository<MongoDbCommentRepository.CommentEntry, String> {

    @Document("comment")
    @JsonPropertyOrder(alphabetic=true)
    class CommentEntry implements CommentRepository.CommentEntry {

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
