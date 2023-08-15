package com.evalvis.blog.post;

public final class Post {

    private final String author;
    private final String title;
    private final String content;

    public Post(String author, String title, String content) {
        this.author = author;
        this.title = title;
        this.content = content;
    }

    public PostRepository.PostEntry create(PostRepository postRepository) {
        return postRepository.save(new PostRepository.PostEntry(author, title, content));
    }
}
