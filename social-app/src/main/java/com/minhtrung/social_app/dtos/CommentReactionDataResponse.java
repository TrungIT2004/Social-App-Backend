package com.minhtrung.social_app.dtos;

import java.util.UUID;

import com.minhtrung.social_app.enums.Reactions;

import lombok.Getter;

@Getter
public class CommentReactionDataResponse {
    private UUID reactionId;
    private UUID userId;
    private Reactions reaction;
    private String fullname;
    private String profilePicUrl;

    public CommentReactionDataResponse(UUID reactionId, UUID userId, Reactions reaction, String fullname,
            String profilePicUrl) {
        this.reactionId = reactionId;
        this.userId = userId;
        this.reaction = reaction;
        this.fullname = fullname;
        this.profilePicUrl = profilePicUrl;
    }
}
