package com.minhtrung.social_app.exceptions;

import java.util.Map;
import java.util.function.BiFunction;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String msg = ex.getMostSpecificCause().getMessage();
        log.warn("Data integrity violation: {}", msg);

        // Helper to build error response
        BiFunction<String, String, ResponseEntity<?>> error = (code, message) ->
            ResponseEntity.status(404).body(Map.of("errorCode", code, "msg", message));

        BiFunction<String, String, ResponseEntity<?>> conflict = (code, message) ->
            ResponseEntity.status(409).body(Map.of("errorCode", code, "msg", message));

        // 1. Friendship constraints
        if (msg.contains("fk_friendship_requester")) {
            return error.apply("REFERENCED_USER_NOT_FOUND", "Requester not found");
        }
        if (msg.contains("fk_friendship_addressee") || msg.contains("uk_friendship_addressee")) {
            return error.apply("REFERENCED_USER_NOT_FOUND", "Addressee not found");
        }
        if (msg.contains("uk_friendship")) {
            return conflict.apply("FRIENDSHIP_ALREADY_EXISTS", "Friendship already exists");
        }

        // 2. Post constraints
        if (msg.contains("fk_post_user")) {
            return error.apply("REFERENCED_USER_NOT_FOUND", "User not exists");
        }

        // 3. Post reaction constraints
        if (msg.contains("fk_post_reaction_interactable")) { 
            return error.apply("REFERENCED_POST_NOT_FOUND", "Post not exists");
        }
        if (msg.contains("uk_user_interactable")) {
            return conflict.apply("REACTION_ALREADY_EXISTS", "User has already reacted to the post");
        }

        // 4. Comment constraints
        if (msg.contains("fk_comment_interactable")) {
            return error.apply("REFERENCED_POST_NOT_FOUND", "Post not exists");
        }
        if (msg.contains("fk_comment_user")) {
            return error.apply("REFERENCED_USER_NOT_FOUND", "User not exists");
        }

        // 5. Comment reaction constraints
        if (msg.contains("fk_comment_reaction_user")) {
            return error.apply("REFERENCED_USER_NOT_FOUND", "User not exists");
        }
        if (msg.contains("fk_comment_reaction_comment")) {
            return error.apply("REFERENCED_COMMENT_NOT_FOUND", "Comment not exists");
        }
        if (msg.contains("uk_user_comment")) {
            return conflict.apply("REACTION_ALREADY_EXISTS", "User already reacted to this comment");
        }

        // 6. Share constraints
        if (msg.contains("fk_share_user")) {
            return error.apply("REFERENCED_USER_NOT_FOUND", "User not exists");
        }
        if (msg.contains("fk_share_post")) {
            return error.apply("REFERENCED_POST_NOT_FOUND", "Referenced post not exists");
        }

        // Fallback for any other DataIntegrityViolation
        return ResponseEntity.status(500).body(Map.of(
            "errorCode", "DATA_INTEGRITY_ERROR",
            "msg", "Database constraint violation"));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex) {
        log.error("Unexpected Error", ex);
        return ResponseEntity.status(500).body("Internal Server Error");
    }
}
