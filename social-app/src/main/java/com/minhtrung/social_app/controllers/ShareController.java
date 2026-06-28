package com.minhtrung.social_app.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minhtrung.social_app.common.context.RequestContext;
import com.minhtrung.social_app.common.context.RequestContextHolder;
import com.minhtrung.social_app.dtos.CreateShareRequest;
import com.minhtrung.social_app.enums.ShareErrorCode;
import com.minhtrung.social_app.exceptions.ShareException;
import com.minhtrung.social_app.models.Share;
import com.minhtrung.social_app.services.ShareService;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/v1/shares")
@Slf4j
public class ShareController {
    private final ShareService shareService;

    public ShareController(ShareService shareService) {
        this.shareService = shareService;
    }

    @GetMapping("/{shareId}")
    public ResponseEntity<?> getSharedPost(@PathVariable UUID shareId) {
        try {
            Object sharedPost = shareService.getOneSharedPost(shareId);
            return ResponseEntity.status(200).body(sharedPost);
        } catch (ShareException ex) {
            ShareErrorCode errorCode = ex.getErrorCode();
            int status = 0;
            String msg = null;

            switch (errorCode) {
                case SHARE_NOT_FOUND:
                    status = 404;
                    msg = "Can't find the shared post";
                    break;
                default:
                    break;
            }

            return ResponseEntity.status(status).body(Map.of(
                "errorCode", errorCode,
                "msg", msg
            ));
        }
    }

    @PostMapping("")
    public ResponseEntity<Share> sharePost(@RequestBody CreateShareRequest req) {
        RequestContext ctx = RequestContextHolder.get();
        log.debug("Request Context: {}", ctx.getUserId());

        req.setUserId(ctx.getUserId());
        Share newShare = shareService.createSharePost(req);
        return ResponseEntity.status(201).body(newShare);
    }

    @DeleteMapping("/{shareId}")
    public ResponseEntity<?> deleteSharedPost(@PathVariable UUID shareId) {
        RequestContext ctx = RequestContextHolder.get();

        try {
            shareService.removeSharedPost(shareId, ctx.getUserId());
            return ResponseEntity.status(200).body("Shared Post deleted");
        } catch (ShareException ex) {
            ShareErrorCode errorCode = ex.getErrorCode();
            int status = 0;
            String msg = null;

            switch (errorCode) {
                case SHARE_NOT_FOUND:
                    status = 404;
                    msg = "Can't find the shared post";
                    break;
                case SHARE_NOT_OWNED:
                    status = 409;
                    msg = "Can't remove others shared post";
                default:
                    break;
            }

            return ResponseEntity.status(status).body(Map.of(
                "errorCode", errorCode,
                "msg", msg
            ));
        }
        
    }
    
    
}
