package com.minhtrung.social_app.dtos;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class FriendDataResponse {
    private UUID friendshipId;
    private UUID userId;
    private String fullname;
    private String profilePicUrl;
    private int mutualFriendsCount = 0;
}
