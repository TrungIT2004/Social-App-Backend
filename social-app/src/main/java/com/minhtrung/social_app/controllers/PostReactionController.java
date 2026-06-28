package com.minhtrung.social_app.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minhtrung.social_app.common.context.RequestContext;
import com.minhtrung.social_app.common.context.RequestContextHolder;
import com.minhtrung.social_app.dtos.CreatePostReactionRequest;
import com.minhtrung.social_app.dtos.PostReactionDataResponse;
import com.minhtrung.social_app.enums.PostReactionErrorCode;
import com.minhtrung.social_app.exceptions.PostReactionException;
import com.minhtrung.social_app.models.PostReaction;
import com.minhtrung.social_app.services.PostReactionService;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/api/v1/post-reaction")
@Slf4j
public class PostReactionController {
    private final PostReactionService postReactionService;

    PostReactionController(PostReactionService postReactionService) {
        this.postReactionService = postReactionService;
    }

    @GetMapping("/{interactableId}")
    public ResponseEntity<List<PostReactionDataResponse>> getPostReactions(@PathVariable UUID interactableId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
            List<PostReactionDataResponse> reactions = postReactionService.retrieveReactionsFromOnePost(interactableId, page, size);
            return ResponseEntity.status(200).body(reactions);
    }
    
    @PostMapping()
    public ResponseEntity<?> createPostReaction(@RequestBody CreatePostReactionRequest createPostReactionRequest) {
        RequestContext ctx = RequestContextHolder.get();

        log.info("Endpoint POST /api/v1/post-reaction called by user {}", ctx.getUserId());

        PostReaction newReaction = postReactionService.insertPostReaction(createPostReactionRequest, ctx.getUserId());
        return ResponseEntity.status(201).body(newReaction);
    }
    
    @PutMapping("/{reactionId}")
    public ResponseEntity<?> updatePostReaction(@PathVariable UUID reactionId, @RequestBody CreatePostReactionRequest createPostReactionRequest) {
        RequestContext ctx = RequestContextHolder.get();

        try {
            log.info("Endpoint PUT /api/v1/post-reaction/{} called by user {}", reactionId, ctx.getUserId());

            PostReaction updatedReaction = postReactionService.changePostReaction(createPostReactionRequest, reactionId, ctx.getUserId());

            return ResponseEntity.status(200).body(updatedReaction);
        } catch (PostReactionException ex) {
            PostReactionErrorCode errorCode = ex.getErrorCode();
            int status = 0;
            String msg = null;

            switch (errorCode) {
                case REACTION_NOT_FOUND:
                    status = 404;
                    msg = "Can't find reaction";
                    break;
                case REACTION_NOT_OWNED:
                    status = 403;
                    msg = "Post reaction doesn't belong to user";
                    break;
                default:
                    break;
            }

            log.error("User {} failed to change reaction {} for post {}", ctx.getUserId(), reactionId, createPostReactionRequest.getInteractableId());
            return ResponseEntity.status(status).body(Map.of(
                "errorCode", errorCode,
                "message", msg));
        } 
    }
    
    @DeleteMapping("/{reactionId}")
    public ResponseEntity<?> deletePostReaction(@PathVariable UUID reactionId) {
        RequestContext ctx = RequestContextHolder.get();

        try {
            log.info("Endpoint DELETE /api/v1/post-reaction/{} called by user {}", reactionId, ctx.getUserId());

            postReactionService.removePostReaction(reactionId, ctx.getUserId());

            return ResponseEntity.status(200).body("Remove reaction");
        } catch (PostReactionException ex) {
            PostReactionErrorCode errorCode = ex.getErrorCode();
            int status = 0;
            String msg = null;

            switch (errorCode) {
                case REACTION_NOT_FOUND:
                    status = 404;
                    msg = "Can't find reaction";
                    break;
                case REACTION_NOT_OWNED:
                    status = 403;
                    msg = "Post reaction doesn't belong to user";
                    break;
                default:
                    break;
            }

            return ResponseEntity.status(status).body(Map.of(
                "errorCode", errorCode,
                "message", msg));
        } 
    }
}
