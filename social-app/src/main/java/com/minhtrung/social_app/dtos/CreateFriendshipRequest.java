
package com.minhtrung.social_app.dtos;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Getter
public class CreateFriendshipRequest {
    @NonNull
    private UUID requesterId;
    
    @NonNull
    private UUID addresseeId;
}
