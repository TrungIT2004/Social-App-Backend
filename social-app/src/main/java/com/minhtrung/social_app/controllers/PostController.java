package com.minhtrung.social_app.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.minhtrung.social_app.common.context.RequestContext;
import com.minhtrung.social_app.common.context.RequestContextHolder;
import com.minhtrung.social_app.dtos.CreatePostRequest;
import com.minhtrung.social_app.dtos.PostDataResponse;
import com.minhtrung.social_app.enums.PostErrorCode;
import com.minhtrung.social_app.exceptions.PostException;
import com.minhtrung.social_app.models.Post;
import com.minhtrung.social_app.services.PostService;

import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {
    private final PostService postService;

    PostController(PostService postService) {
        this.postService = postService;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping()
    public ResponseEntity<List<PostDataResponse>> getPosts(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<PostDataResponse> posts = postService.retrievePosts(page, size);
        return ResponseEntity.status(200).body(posts);
    }

    @GetMapping("/hashtags/{hashtagName}")
    public ResponseEntity<List<PostDataResponse>> getPostsFromHashTag(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, @PathVariable String hashtagName) {
        List<PostDataResponse> posts = postService.retrieveAllPostsFromOneHashtag(page, size, hashtagName);
        return ResponseEntity.status(200).body(posts);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPost(@RequestPart(value = "post", required = false) String postJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {

        RequestContext ctx = RequestContextHolder.get();

        CreatePostRequest postRequest = objectMapper.readValue(postJson, CreatePostRequest.class);

        Post createdPost = postService.insertPost(postRequest, ctx.getUserId(), files);
        return ResponseEntity.status(201).body(createdPost);
    }
    
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable UUID postId) {
        try {
            RequestContext ctx = RequestContextHolder.get();
            postService.removePost(postId, ctx.getUserId());
            return ResponseEntity.ok().body("Post deleted");
        } catch (PostException ex) {
            PostErrorCode errorCode = ex.getErrorCode();
            int status = 0;
            String msg = null;

            switch (errorCode) {
                case POST_NOT_FOUND:
                    status = 404;
                    msg = "Can't find the post";
                    break;
                case POST_NOT_OWNED:
                    status = 403;
                    msg = "Post doesn't belong to user";
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
    
}
