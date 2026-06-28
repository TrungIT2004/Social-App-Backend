package com.minhtrung.social_app.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minhtrung.social_app.common.context.RequestContext;
import com.minhtrung.social_app.common.context.RequestContextHolder;
import com.minhtrung.social_app.dtos.CommentReactionDataResponse;
import com.minhtrung.social_app.dtos.CreateCommentReactionRequest;
import com.minhtrung.social_app.enums.CommentReactionErrorCode;
import com.minhtrung.social_app.exceptions.CommentReactionException;
import com.minhtrung.social_app.models.CommentReaction;
import com.minhtrung.social_app.services.CommentReactionService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("/api/v1/comment-reaction")
@Slf4j
public class CommentReactionController {
    private CommentReactionService commentReactionService;

    public CommentReactionController(CommentReactionService commentReactionService) {
        this.commentReactionService = commentReactionService;
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<List<CommentReactionDataResponse>> getPostReactions(@PathVariable UUID commentId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
            List<CommentReactionDataResponse> reactions = commentReactionService.retrieveReactionsFromOneComment(commentId, page, size);
            return ResponseEntity.status(200).body(reactions);
        
    }

    @PostMapping("")
    public ResponseEntity<?> createReaction(@RequestBody CreateCommentReactionRequest req) {
        RequestContext ctx = RequestContextHolder.get();
        req.setUserId(ctx.getUserId());
        log.debug("Userid : {}", ctx.getUserId());

        CommentReaction reaction = commentReactionService.insertReaction(req);
        return ResponseEntity.status(201).body(reaction);
    }
    
    @PutMapping("/{reactionId}")
    public ResponseEntity<?> updateReaction(@PathVariable UUID reactionId,
            @RequestBody CreateCommentReactionRequest req) {
        RequestContext ctx = RequestContextHolder.get();
        req.setUserId(ctx.getUserId());

        try {
            CommentReaction updatedReaction = commentReactionService.changeReaction(reactionId, req);
            return ResponseEntity.status(200).body(updatedReaction);
        } catch (CommentReactionException ex) {
            CommentReactionErrorCode errorCode = ex.getErrorCode();
            int status = 0;
            String msg = null;

            switch (errorCode) {
                case REACTION_NOT_FOUND:
                    status = 404;
                    msg = "Can't find referenced comment";
                    break;
                case REACTION_NOT_OWNED:
                    status = 403;
                    msg = "Can't update other user's reaction";
                    break;
                default:
                    break;
            }

            return ResponseEntity.status(status).body(Map.of(
                    "errorCode", errorCode,
                    "msg", msg));
        } 
    }
    
    @DeleteMapping("/{reactionId}")
    public ResponseEntity<?> deleteReaction(@PathVariable UUID reactionId) {
        RequestContext ctx = RequestContextHolder.get();

        try {
            commentReactionService.removeReaction(reactionId, ctx.getUserId());
            return ResponseEntity.status(200).body("Reaction deleted");
        } catch (CommentReactionException ex) {
            CommentReactionErrorCode errorCode = ex.getErrorCode();
            int status = 0;
            String msg = null;

            switch (errorCode) {
                case REACTION_NOT_FOUND:
                    status = 404;
                    msg = "Can't find referenced comment";
                    break;
                case REACTION_NOT_OWNED:
                    status = 403;
                    msg = "Can't update other user's reaction";
                    break;
                default:
                    break;
            }

            return ResponseEntity.status(status).body(Map.of(
                    "errorCode", errorCode,
                    "msg", msg));
        } 
    }
    
}
