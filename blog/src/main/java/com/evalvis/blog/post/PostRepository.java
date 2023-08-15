package com.evalvis.blog.post;

import jakarta.persistence.*;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.CrudRepository;

@Repository
public interface PostRepository extends CrudRepository<PostRepository.PostEntry, Long> {

    @Entity(name = "post")
    class PostEntry {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        @Column(unique = true)
        private String id;
        @Column(nullable = false)
        private String author;
        @Column(nullable = false)
        private String title;
        @Column(nullable = false)
        private String content;

        PostEntry(String author, String title, String content) {
            this.author = author;
            this.title = title;
            this.content = content;
        }

        public PostEntry() {}

        public String getId() {
            return id;
        }

        public String getAuthor() {
            return author;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }
    }
}
