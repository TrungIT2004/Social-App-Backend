package com.minhtrung.social_app.services;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.minhtrung.social_app.dtos.CreateShareRequest;
import com.minhtrung.social_app.dtos.ShareDataResponse;
import com.minhtrung.social_app.enums.ShareErrorCode;
import com.minhtrung.social_app.exceptions.ShareException;
import com.minhtrung.social_app.models.Post;
import com.minhtrung.social_app.models.Share;
import com.minhtrung.social_app.models.User;
import com.minhtrung.social_app.repositories.PostRepository;
import com.minhtrung.social_app.repositories.ShareRepository;
import com.minhtrung.social_app.repositories.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ShareService {
    private final ShareRepository shareRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public ShareService(ShareRepository shareRepository, PostRepository postRepository, UserRepository userRepository) {
        this.shareRepository = shareRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public ShareDataResponse getOneSharedPost(UUID shareId) {
        return shareRepository.retrieveOneSharedPost(shareId)
                .orElseThrow(() -> {
                    log.warn("Shared post {} not exists", shareId);
                    throw new ShareException(ShareErrorCode.SHARE_NOT_FOUND, "Share Not Found");
                });
    }
    
    public Share createSharePost(CreateShareRequest req) {
        Share newShare = new Share();
        Post proxyPost = postRepository.getReferenceById(req.getPostId());
        User proxyUser = userRepository.getReferenceById(req.getUserId());

        newShare.setPost(proxyPost);
        newShare.setUser(proxyUser);
        newShare.setTextContent(req.getTextContent());
        newShare.setVisibility(req.getVisibility());

        return shareRepository.save(newShare);
    }

    public void removeSharedPost(UUID shareId, UUID userId) {
        Share sharedPost = shareRepository.findById(shareId).orElseThrow( () -> {
            log.warn("Shared post {} not exists", shareId);
            throw new ShareException(ShareErrorCode.SHARE_NOT_FOUND, "Share Not Found");
        });

        if (!sharedPost.getUserId().equals(userId)) {
            log.warn("Shared Post {} doesnt belong to user {}", shareId, userId);
            throw new ShareException(ShareErrorCode.SHARE_NOT_OWNED, "Shared Post not owned");
        }

        shareRepository.delete(sharedPost);
    }
}
