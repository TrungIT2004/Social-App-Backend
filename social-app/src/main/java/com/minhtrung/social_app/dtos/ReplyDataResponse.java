package com.minhtrung.social_app.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

import com.minhtrung.social_app.models.Comment;

import lombok.Data;

@Data
public class ReplyDataResponse {
    private UUID commentId;
    private UUID parentCommentId;
    private UUID userId;
    private UUID interactableId;
    private String textContent;
    private String mediaUrl;
    private LocalDateTime createdAt;
    private int reactionCount;
    private int replyCount;

    private String fullname;
    private String profilePicUrl;

    public ReplyDataResponse(Comment comment, String fullName, String profilePicUrl) {
        this.commentId = comment.getCommentId();
        this.parentCommentId = comment.getParentCommentId();
        this.userId = comment.getUserId();
        this.interactableId = comment.getInteractableId();
        this.textContent = comment.getTextContent();
        this.mediaUrl = comment.getMediaUrl();
        this.reactionCount = comment.getReactionCount();
        this.replyCount = comment.getReplyCount();

        this.fullname = fullName;
        this.profilePicUrl = profilePicUrl;
    }
}
