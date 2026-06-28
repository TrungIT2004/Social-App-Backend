package com.minhtrung.social_app.services;

import com.minhtrung.social_app.repositories.ShareRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minhtrung.social_app.dtos.CreatePostReactionRequest;
import com.minhtrung.social_app.dtos.PostReactionDataResponse;
import com.minhtrung.social_app.enums.InteractableType;
import com.minhtrung.social_app.enums.PostReactionErrorCode;
import com.minhtrung.social_app.enums.Reactions;
import com.minhtrung.social_app.exceptions.PostReactionException;
import com.minhtrung.social_app.models.Interactable;
import com.minhtrung.social_app.models.PostReaction;
import com.minhtrung.social_app.models.User;
import com.minhtrung.social_app.repositories.InteractableRepository;
import com.minhtrung.social_app.repositories.PostReactionRepository;
import com.minhtrung.social_app.repositories.PostRepository;
import com.minhtrung.social_app.repositories.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PostReactionService {
    private final ShareRepository shareRepository;
    private final PostReactionRepository postReactionRepository;
    private final InteractableRepository interactableRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    PostReactionService(PostReactionRepository postReactionRepository, InteractableRepository interactableRepository , PostRepository postRepository, UserRepository userRepository, ShareRepository shareRepository) {
        this.postReactionRepository = postReactionRepository;
        this.interactableRepository = interactableRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.shareRepository = shareRepository;
    }
    
    public List<PostReactionDataResponse> retrieveReactionsFromOnePost(UUID interactableId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> data = postReactionRepository.getReactionsFromOnePost(pageable, interactableId);
        List<PostReactionDataResponse> reactions = data.stream().map(row -> {
            UUID reactionId = (UUID) row[0];
            UUID userId = (UUID) row[1];
            Reactions reaction = (Reactions) row[2]; 
            String fullname = (String) row[3] + " " + row[4];
            String profilePicUrl = (String) row[5];
            return new PostReactionDataResponse(reactionId, userId, reaction, fullname, profilePicUrl);
        })
                .collect(Collectors.toList());
        return reactions;
    }

    @Transactional
    public PostReaction insertPostReaction(CreatePostReactionRequest req, UUID userId) {
        Reactions reaction = req.getReaction();
        UUID interactableId = req.getInteractableId();

        PostReaction newReaction = new PostReaction();
        Interactable interactableProxy = interactableRepository.getReferenceById(interactableId);
        User userProxy = userRepository.getReferenceById(userId);
        newReaction.setInteractable(interactableProxy);
        newReaction.setReaction(reaction);
        newReaction.setUser(userProxy);
        
        Interactable interactable = interactableRepository.findById(interactableId).orElseThrow(() -> {
            log.warn("Can't find interactable {}", interactableId);
            throw new RuntimeException("Can't find interactable");
        });

        if (interactable.getType() == InteractableType.post) {
            postRepository.increaseReactionCount(interactableId);
        } else if (interactable.getType() == InteractableType.share) {
            shareRepository.incrementReactionCount(interactableId);
        }

        PostReaction res = postReactionRepository.save(newReaction);
        postRepository.increaseReactionCount(interactableId);

        log.info("User {} reacted to post", userId);
        log.debug("Create a new reaction and save in DB: {}", res);

        return res;
    } 
    

    @Transactional
    public PostReaction changePostReaction(CreatePostReactionRequest req, UUID reactionId, UUID userId) {
        Reactions reactionText = req.getReaction();

        PostReaction reaction = postReactionRepository.findById(reactionId).orElseThrow(() -> {
            log.warn("Post reaction {} not found", reactionId);
            throw new PostReactionException(PostReactionErrorCode.REACTION_NOT_FOUND, "Reaction Not Found");
        });

        if (!reaction.getUserId().equals(userId)) {
            log.warn("Post reaction {} doesn't belong to user {}", reactionId, userId);
            throw new PostReactionException(PostReactionErrorCode.REACTION_NOT_OWNED,
                    "Reaction doesn't belong to user");
        }

        reaction.setReaction(reactionText);
        PostReaction updatedReaction = postReactionRepository.save(reaction);

        log.info("User {} changed reaction for post", userId);
        log.debug("Updated post reaction {} to DB: {}", reactionId, reaction);
        return updatedReaction;
    }
    
    @Transactional
    public void removePostReaction(UUID reactionId, UUID userId) {
        PostReaction reaction = postReactionRepository.findById(reactionId).orElseThrow(() -> {
            log.warn("Can't find post reaction {}", reactionId);
            throw new PostReactionException(PostReactionErrorCode.REACTION_NOT_FOUND, "Can't find reaction");
        });

        if (!reaction.getUserId().equals(userId)) {
            log.warn("Post reaction {} doesn't belong to user {}", reactionId, userId);
            throw new PostReactionException(PostReactionErrorCode.REACTION_NOT_OWNED,
                    "Reaction doesn't belong to user");
        }
        
        UUID interactableId = reaction.getInteractableId();
        Interactable interactable = interactableRepository.findById(interactableId).orElseThrow(() -> {
            log.warn("Can't find interactable {}", interactableId);
            throw new RuntimeException("Can't find interactable");
        });

        if (interactable.getType() == InteractableType.post) {
            postRepository.decreaseReactionCount(interactableId);
        } else if (interactable.getType() == InteractableType.share) {
            shareRepository.decrementReactionCount(interactableId);
        }

        postReactionRepository.delete(reaction);
        
        log.info("User {} deleted post reaction {}", userId, reactionId);
        log.debug("Deleted post reaction {} from DB", reaction);
    }
}
