package com.minhtrung.social_app.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.minhtrung.social_app.dtos.CreateFriendshipRequest;
import com.minhtrung.social_app.dtos.FriendDataResponse;
import com.minhtrung.social_app.dtos.FriendRecommendationDataResponse;
import com.minhtrung.social_app.dtos.FriendRequestDataResponse;
import com.minhtrung.social_app.models.Friendship;
import com.minhtrung.social_app.services.FriendshipService;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;




@RestController
@RequestMapping("/api/v1/friends")
@Slf4j
public class FriendshipController {
    private final FriendshipService friendshipService;

    FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @GetMapping("/requests/to/{userId}")
    public ResponseEntity<List<FriendRequestDataResponse>> getFriendRequests(@PathVariable UUID userId) {
        List<FriendRequestDataResponse> friendRequests = friendshipService.getFriendRequests(userId);
        return ResponseEntity.status(200).body(friendRequests);
    }

    @GetMapping("/requests/from/{userId}")
    public ResponseEntity<List<FriendRequestDataResponse>> getSentFriendRequests(@PathVariable UUID userId) {
        List<FriendRequestDataResponse> friendRequests = friendshipService.getSentFriendRequests(userId);
        return ResponseEntity.status(200).body(friendRequests);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<FriendDataResponse>> getFriends(@PathVariable UUID userId) {
        List<FriendDataResponse> friendRequests = friendshipService.getUsersFriends(userId);
        return ResponseEntity.status(200).body(friendRequests);
    }

    @GetMapping("/recommendations/{algorithm}/{userId}")
    public ResponseEntity<List<FriendRecommendationDataResponse>> getFriendSuggestions(@PathVariable String algorithm,
            @PathVariable UUID userId, @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        List<FriendRecommendationDataResponse> suggestions = null;

        if (algorithm.equals("adamic-adar")) {
            suggestions = friendshipService.getFriendsRecommendations_AdamicAdar(page, size, userId);
        } else if (algorithm.equals("jaccard")) {
            suggestions = friendshipService.getFriendsRecommendations_Jaccard(page, size, userId);
        }

        return ResponseEntity.status(200).body(suggestions);
    }
    
    @PostMapping("")
    public ResponseEntity<Friendship> CreateFriendRequest(@RequestBody CreateFriendshipRequest req) {
        Friendship newFriendship = friendshipService.createFriendRequest(req);
        return ResponseEntity.status(201).body(newFriendship);
    }

    @PutMapping("/requests/accept/{friendshipId}")
    public ResponseEntity<Friendship> acceptFriendRequest(@PathVariable UUID friendshipId) {
        Friendship rejectedFriendship = friendshipService.acceptFriendRequest(friendshipId);
        return ResponseEntity.status(200).body(rejectedFriendship);
    }
    
    @PutMapping("/requests/reject/{friendshipId}")
    public ResponseEntity<Friendship> rejectFriendRequest(@PathVariable UUID friendshipId) {
        Friendship rejectedFriendship = friendshipService.rejectFriendRequest(friendshipId);
        return ResponseEntity.status(200).body(rejectedFriendship);
    }

    @PutMapping("/unfriend/{friendshipId}")
    public ResponseEntity<Friendship> unfriendUser(@PathVariable UUID friendshipId) {
        Friendship rejectedFriendship = friendshipService.removeUserFromFriendList(friendshipId);
        return ResponseEntity.status(200).body(rejectedFriendship);
    }
}
