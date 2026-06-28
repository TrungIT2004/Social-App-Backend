package com.minhtrung.social_app.dtos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.minhtrung.social_app.models.Post;

import lombok.Getter;

@Getter
public class PostDataResponse {
    private UUID postId;
    private UUID userId;
    private UUID interactableId;
    private String text;
    private List<String> mediaUrls;
    private int reactionCount;
    private int commentCount;
    private int shareCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String fullname;
    private String profilePicUrl;

    public PostDataResponse(Post post, String fullname, String profilePicUrl) {
        this.postId = post.getPostId();
        this.userId = post.getUserId();
        this.interactableId = post.getInteractableId();
        this.text = post.getText();
        this.mediaUrls = post.getMediaUrls();
        this.reactionCount = post.getReactionCount();
        this.commentCount = post.getCommentCount();
        this.shareCount = post.getShareCount();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdateddAt();
        this.fullname = fullname;
        this.profilePicUrl = profilePicUrl;
    }
}
