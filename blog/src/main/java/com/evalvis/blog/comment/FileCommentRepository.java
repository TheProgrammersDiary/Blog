package com.evalvis.blog.comment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Repository("fileCommentRepository")
public class FileCommentRepository implements CommentRepository {

    private final Supplier<File> commentRepo;

    public FileCommentRepository() {
        commentRepo = FileCommentRepository::initRepo;
    }

    private static File initRepo() {
        File repo = Paths.get("repo/commentRepository.json").toFile();
        if(!repo.exists()) {
            try {
                repo.createNewFile();
                try(BufferedWriter writer = new BufferedWriter(new FileWriter(repo))) {
                    writer.write("[]");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return repo;
    }

    @Override
    public List<CommentRepository.CommentEntry> findCommentEntriesByPostEntryId(String postId) {
        try {
            List<CommentEntry> comments = new ObjectMapper()
                    .readValue(commentRepo.get(), new TypeReference<>() {});
            return comments
                    .stream()
                    .filter(comment -> comment.getPostEntryId().equals(postId))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CommentRepository.CommentEntry save(CommentRepository.CommentEntry entry) {
        try {
            List<CommentEntry> comments = new ObjectMapper()
                    .readValue(commentRepo.get(), new TypeReference<>() {});
            comments.add((CommentEntry) entry);
            new ObjectMapper().writeValue(commentRepo.get(), comments);
            return entry;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @JsonPropertyOrder({"author", "content", "id", "postEntryId"})
    public static final class CommentEntry implements CommentRepository.CommentEntry {

        private final String id;
        private final String author;
        private final String content;
        private final String postEntryId;

        @JsonCreator
        CommentEntry(
                @JsonProperty("author") String author, @JsonProperty("content") String content,
                @JsonProperty("postEntryId") String postEntryId
        ) {
            id = UUID.randomUUID().toString();
            this.author = author;
            this.content = content;
            this.postEntryId = postEntryId;
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
