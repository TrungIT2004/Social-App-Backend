package com.minhtrung.social_app.dtos;

import java.util.UUID;

import lombok.Data;

@Data
public class CreateCommentRequest {
    private UUID interactableId;
    private UUID parentCommentId = null;
    private String textContent;
}
