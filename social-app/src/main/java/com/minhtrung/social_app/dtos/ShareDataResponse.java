package com.minhtrung.social_app.dtos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.minhtrung.social_app.enums.Visibility;
import com.minhtrung.social_app.models.Share;

import lombok.Getter;

@Getter
public class ShareDataResponse {
    private UUID postId;
    private UUID userId;
    private UUID shareId;
    private UUID interactableId;
    private String sharetextContent;
    private Visibility visibility;
    private int reactionCount;
    private int commentCount;
    private String sharerFullname;
    private String sharerprofilePicUrl;
    private LocalDateTime shareCreatedAt;

    // Original Post Data
    private String postTextContent;
    private List<String> mediaUrls;
    private LocalDateTime postCreatedAt;
    private String ownerFullname;
    private String ownerProfilePicUrl;

    public ShareDataResponse(Share share, String sharerFullname, String sharerProfilePicUrl,
            String postTextContent, List<String> mediaUrls, LocalDateTime postCreatedAt,
                        String ownerFullname, String ownerProfilePicUrl) {
        this.postId = share.getPostId();
        this.userId = share.getUserId();
        this.shareId = share.getShareId();
        this.interactableId = share.getInteractableId();
        this.sharetextContent = share.getTextContent();
        this.visibility = share.getVisibility();
        this.reactionCount = share.getReactionCount();
        this.commentCount = share.getCommentCount();
        this.sharerFullname = sharerFullname;
        this.sharerprofilePicUrl = sharerProfilePicUrl;
        this.shareCreatedAt = share.getCreatedAt();

        this.postTextContent = postTextContent;
        this.mediaUrls = mediaUrls;
        this.postCreatedAt = postCreatedAt;
        this.ownerFullname = ownerFullname;
        this.ownerProfilePicUrl = ownerProfilePicUrl;
    }
}
