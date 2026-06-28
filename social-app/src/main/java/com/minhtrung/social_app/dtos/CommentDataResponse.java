package com.minhtrung.social_app.dtos;

import java.time.LocalDateTime;
import java.util.UUID;
import com.minhtrung.social_app.models.Comment;

import lombok.Getter;

@Getter
public class CommentDataResponse {
    private UUID userId;
    private UUID commentId;
    private UUID interactableId;
    private String textContent;
    private String mediaUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int reactionCount;
    private int replyCount;
    private String fullname;
    private String profilePicUrl;

    public CommentDataResponse(Comment comment, String fullname, String profilePicUrl) {
        this.userId = comment.getUserId();
        this.commentId = comment.getCommentId();
        this.interactableId = comment.getInteractableId();
        this.textContent = comment.getTextContent();
        this.mediaUrl = comment.getMediaUrl();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
        this.reactionCount = comment.getReactionCount();
        this.replyCount = comment.getReplyCount();
        this.fullname = fullname;
        this.profilePicUrl = profilePicUrl;
    }
}
