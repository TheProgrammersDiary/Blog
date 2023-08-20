package com.evalvis.blog.comment;

import com.evalvis.blog.post.PostRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends CrudRepository<CommentRepository.CommentEntry, String> {

    List<CommentEntry> findCommentEntriesByPostEntryId(String postId);

    @Entity(name = "comment")
    class CommentEntry {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        @Column(unique = true)
        private String id;
        @Column(nullable = false)
        private String author;
        @Column(nullable = false)
        private String content;
        @ManyToOne(fetch = FetchType.LAZY)
        @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
        private PostRepository.PostEntry postEntry;

        CommentEntry(String author, String content, String postId) {
            this.author = author;
            this.content = content;
            postEntry = new PostRepository.PostEntry(postId);
        }

        public CommentEntry() {}

        public String getId() {
            return id;
        }

        public String getAuthor() {
            return author;
        }

        public String getContent() {
            return content;
        }
    }
}
