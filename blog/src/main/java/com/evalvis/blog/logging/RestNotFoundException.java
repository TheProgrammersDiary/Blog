package com.evalvis.blog.logging;

public class RestNotFoundException extends RuntimeException {
    public RestNotFoundException(String message) {
        super(message);
    }
}
