package com.minhtrung.social_app.exceptions;

import java.util.Map;

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

        if (msg.contains("fk_friendship_requester")) {
            return ResponseEntity.status(404).body(Map.of(
                "errorCode", "REFERENCED_USER_NOT_FOUND",
                "msg", "Requester not found"));
        } else if (msg.contains("uk_friendship_addressee")) {
            return ResponseEntity.status(404).body(Map.of(
            "errorCode", "REFERENCED_USER_NOT_FOUND",
            "msg", "Addressee not found"));
        } else if (msg.contains("uk_friendship")) {
            return ResponseEntity.status(404).body(Map.of(
            "errorCode", "FRIENDSHIP_ALREADY_EXISTS",
            "msg", "Friendship already exists"));
        }

        // Handle Data Integrity Violation for post
        if (msg.contains("fk_post_user")) {
            return ResponseEntity.status(404).body(Map.of(
                "errorCode", "REFERENCED_USER_NOT_FOUND",
                "msg", "User not exists"));
        }
        
        // Handle Data Integrity Violation for post reaction
        if (msg.contains("fk_post_reaction_interactablet")) {
            return ResponseEntity.status(404).body(Map.of(
                "errorCode", "REFERENCED_POST_NOT_FOUND",
                "msg", "Post not exists"));
        } else if (msg.contains("uk_user_interactable")) {
            return ResponseEntity.status(404).body(Map.of(
            "errorCode", "REACTION_ALREADY_EXISTS",
            "msg", "User has already reacted to the post"));
        }

         // Handle Data Integrity Violation for comment 
        if (msg.contains("fk_comment_interactable")) {
            return ResponseEntity.status(404).body(Map.of(
                "errorCode", "REFERENCED_POST_NOT_FOUND",
                "msg", "Post not exists"));
        } else if (msg.contains("fk_comment_user")) {
            return ResponseEntity.status(404).body(Map.of(
                    "errorCode", "REFERENCED_USER_NOT_FOUND",
                    "msg", "User not exists"));
        }
        
        // Handle Data Integrity Violation for comment reaction
        if (msg.contains("fk_comment_reaction_user")) {
            return ResponseEntity.status(404).body(Map.of(
                "errorCode", "REFERENCED_USER_NOT_FOUND",
                "msg", "User not exists"));
        } else if (msg.contains("fk_comment_reaction_comment")) {
            return ResponseEntity.status(404).body(Map.of(
                "errorCode", "REFERENCED_COMMENT_NOT_FOUND",
                "msg", "Comment not exists"));
        } else if (msg.contains("uk_user_comment")) {
            return ResponseEntity.status(409).body(Map.of(
                    "errorCode", "REACTION_ALREADY_EXISTS",
                    "msg", "User already reacted to this comment"));
        }
        
        // Handle Data Integrity Violation for comment reaction
         if (msg.contains("fk_share_user")) {
            return ResponseEntity.status(404).body(Map.of(
                "errorCode", "REFERENCED_USER_NOT_FOUND",
                "msg", "User not exists"));
        } else if (msg.contains("fk_share_post")) {
            return ResponseEntity.status(404).body(Map.of(
                "errorCode", "REFERENCED_POST_NOT_FOUND",
                "msg", "Referenced post not exists"));
        }

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
