package com.minhtrung.social_app.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minhtrung.social_app.dtos.CreateFriendshipRequest;
import com.minhtrung.social_app.dtos.FriendDataResponse;
import com.minhtrung.social_app.dtos.FriendRequestDataResponse;
import com.minhtrung.social_app.enums.FriendshipStatus;
import com.minhtrung.social_app.models.Friendship;
import com.minhtrung.social_app.models.User;
import com.minhtrung.social_app.repositories.FriendshipRepository;
import com.minhtrung.social_app.repositories.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FriendshipService {
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    FriendshipService(FriendshipRepository friendshipRepository, UserRepository userRepository) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
    }

    public List<FriendRequestDataResponse> getFriendRequests(UUID userId) {
        List<Object[]> friendsData = friendshipRepository.retrieveFriendRequests(userId);

        List<FriendRequestDataResponse> friendRequests = friendsData.stream().map(row -> {
            UUID friendshipId = (UUID) row[5];    
            UUID pendingUserId = (UUID) row[0];
            String fullname = (String) row[1];
            String profilePicUrl = (String) row[2];
            int mutualFriendCount = row[3] != null ? ((Number) row[3]).intValue() : 0;
            LocalDateTime createdAt = (LocalDateTime) row[4];
            return new FriendRequestDataResponse(friendshipId, pendingUserId, fullname, profilePicUrl, mutualFriendCount, createdAt);
        })
                .collect(Collectors.toList());

        return friendRequests;
    }

    public List<FriendRequestDataResponse> getSentFriendRequests(UUID userId) {
        List<Object[]> friendsData = friendshipRepository.retrieveSentFriendRequests(userId);

        List<FriendRequestDataResponse> sentFriendRequests = friendsData.stream().map(row -> {
            UUID friendshipId = (UUID) row[5];
            UUID pendingUserId = (UUID) row[0];
            String fullname = (String) row[1];
            String profilePicUrl = (String) row[2];
            int mutualFriendCount = row[3] != null ? ((Number) row[3]).intValue() : 0;
            LocalDateTime createdAt = (LocalDateTime) row[4];
            return new FriendRequestDataResponse(friendshipId, pendingUserId, fullname, profilePicUrl, mutualFriendCount, createdAt);
        })
                .collect(Collectors.toList());

        return sentFriendRequests;
    }

    public List<FriendDataResponse> getUsersFriends(UUID userId) {
        List<Object[]> friendsData = friendshipRepository.retrieveFriendsInfo(userId);

        List<FriendDataResponse> friends = friendsData.stream().map(row -> {
            UUID friendshipId = (UUID) row[4];
            UUID friendId = (UUID) row[0];
            String fullname = (String) row[1];
            String profilePicUrl = (String) row[2];
            int mutualFriendCount = row[3] != null ? ((Number) row[3]).intValue() : 0;
            return new FriendDataResponse(friendshipId, friendId, fullname, profilePicUrl, mutualFriendCount);
        })
                .collect(Collectors.toList());

        return friends;
    }
    
    public Friendship createFriendRequest(CreateFriendshipRequest req) {
        UUID requesterId = req.getRequesterId();
        UUID addresseeId = req.getAddresseeId();

        if (friendshipRepository.existsFriendshipBetweenUsers(requesterId, addresseeId)) {
            log.warn("Friendship between two users already established");
            throw new RuntimeException("Friendship between two users already established");
        }

        Friendship newFriendship = new Friendship();
        User requesterProxy = userRepository.getReferenceById(requesterId);
        User addresseeProxy = userRepository.getReferenceById(addresseeId);
        newFriendship.setRequester(requesterProxy);
        newFriendship.setAddressee(addresseeProxy);

        return friendshipRepository.save(newFriendship);
    }

    @Transactional
    public Friendship acceptFriendRequest(UUID friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId).orElseThrow(() -> {
            log.warn("Friendship {} cant be found", friendshipId);
            throw new RuntimeException();
        });

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        return friendshipRepository.save(friendship);
    }

    @Transactional
    public Friendship rejectFriendRequest(UUID friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId).orElseThrow(() -> {
            log.warn("Friendship {} cant be found", friendshipId);
            throw new RuntimeException();
        });

        friendship.setStatus(FriendshipStatus.REJECTED);
        return friendshipRepository.save(friendship);
    }

    @Transactional
    public Friendship removeUserFromFriendList(UUID friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId).orElseThrow(() -> {
            log.warn("Friendship {} cant be found", friendshipId);
            throw new RuntimeException();
        });

        friendship.setStatus(FriendshipStatus.UNFRIENDED);
        return friendshipRepository.save(friendship);
    }
}
