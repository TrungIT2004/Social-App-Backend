package com.minhtrung.social_app.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.minhtrung.social_app.common.context.RequestContext;
import com.minhtrung.social_app.common.context.RequestContextHolder;
import com.minhtrung.social_app.dtos.CommentDataResponse;
import com.minhtrung.social_app.dtos.CreateCommentRequest;
import com.minhtrung.social_app.dtos.ReplyDataResponse;
import com.minhtrung.social_app.enums.CommentErrorCode;
import com.minhtrung.social_app.exceptions.CommentException;
import com.minhtrung.social_app.models.Comment;
import com.minhtrung.social_app.services.CommentService;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/v1/comments")
@Slf4j
public class CommentController {
    private final CommentService commentService;

    CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/{interactableId}")
    public ResponseEntity<?> getComments(@PathVariable UUID interactableId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        List<CommentDataResponse> comments = commentService.retrieveCommentsFromOnePost(page, size, interactableId);
        return ResponseEntity.status(200).body(comments);
    }
    
    @GetMapping("/replys")
    public ResponseEntity<?> getMethodName(@RequestParam UUID commentId) {
        List<ReplyDataResponse> replys = commentService.retrieveAllReplysForOneComment(commentId);
        return ResponseEntity.status(200).body(replys);
    }
    

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createComment(@RequestPart(value = "file", required = false) MultipartFile file, @RequestPart(value = "comment") String commentJson) {
        RequestContext ctx = RequestContextHolder.get();
        UUID userId = ctx.getUserId();

        CreateCommentRequest comment = objectMapper.readValue(commentJson, CreateCommentRequest.class);

        try {
            log.info("Endpoint POST /api/v1/comments called by user {}", ctx.getUserId());
            Comment createdComment = commentService.insertComment(comment, userId, file);
            return ResponseEntity.status(201).body(createdComment);
        } catch (CommentException ex) {
            CommentErrorCode errorCode = ex.getErrorCode();
            int status = 0;
            String msg = null;

            switch (errorCode) {
                case NO_CONTENT_ERROR:
                    status = 400;
                    msg = "Missing content in comment";
                    break;
                default:
                    break;
            }

            return ResponseEntity.status(status).body(Map.of(
                "errorCode", errorCode,
                "messsage", msg));
        } 
    }
    
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable UUID commentId) {
        RequestContext ctx = RequestContextHolder.get();
        UUID userId = ctx.getUserId();

        try {
            commentService.removeComment(commentId, userId);
            return ResponseEntity.status(200).body("Comment is deleted");
        } catch (CommentException ex) {
            CommentErrorCode errorCode = ex.getErrorCode();
            int status = 0;
            String msg = null;

            switch (errorCode) {
                case COMMENT_NOT_FOUND:
                    status = 404;
                    msg = "Can't find the comment";
                    break;
                case COMMENT_NOT_OWNED:
                    status = 403;
                    msg = "Comment isn't made by user";
                    break;
                default:
                    break;
            }

            return ResponseEntity.status(status).body(Map.of(
                    "errorCode", errorCode,
                    "messsage", msg));
        } 
    }
    
}
