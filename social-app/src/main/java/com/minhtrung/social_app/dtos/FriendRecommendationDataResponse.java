package com.minhtrung.social_app.dtos;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FriendRecommendationDataResponse {
    private UUID userId;
    private String fullname;
    private String profilePicUrl;
    private int mutual_friend_count;
}
