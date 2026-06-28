package com.minhtrung.social_app.dtos;

import java.util.UUID;

import com.minhtrung.social_app.enums.Reactions;

import lombok.Data;

@Data
public class CreateCommentReactionRequest {
    private UUID userId;
    private UUID commentId;
    private Reactions reaction;
}
