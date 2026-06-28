package com.minhtrung.social_app.dtos;

import java.util.UUID;

import com.minhtrung.social_app.enums.Visibility;

import lombok.Data;

@Data
public class CreateShareRequest {
    private UUID postId;
    private UUID userId;
    private String textContent;
    private Visibility visibility;
}
