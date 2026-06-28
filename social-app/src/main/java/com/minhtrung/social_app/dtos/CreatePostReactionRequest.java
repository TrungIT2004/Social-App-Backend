package com.minhtrung.social_app.dtos;

import java.util.UUID;

import com.minhtrung.social_app.enums.Reactions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CreatePostReactionRequest {
    private UUID interactableId;
    private Reactions reaction;
}
