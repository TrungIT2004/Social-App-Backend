package com.minhtrung.social_app.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.minhtrung.social_app.dtos.CommentReactionDataResponse;
import com.minhtrung.social_app.dtos.CreateCommentReactionRequest;
import com.minhtrung.social_app.enums.CommentReactionErrorCode;
import com.minhtrung.social_app.enums.Reactions;
import com.minhtrung.social_app.exceptions.CommentReactionException;
import com.minhtrung.social_app.models.Comment;
import com.minhtrung.social_app.models.CommentReaction;
import com.minhtrung.social_app.models.User;
import com.minhtrung.social_app.repositories.CommentReactionRepository;
import com.minhtrung.social_app.repositories.CommentRepository;
import com.minhtrung.social_app.repositories.UserRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CommentReactionService {
    private CommentReactionRepository commentReactionRepository;
    private CommentRepository commentRepository;
    private UserRepository userRepository;

    public CommentReactionService(CommentReactionRepository commentReactionRepository,
            CommentRepository commentRepository, UserRepository userRepository) {
        this.commentReactionRepository = commentReactionRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    public List<CommentReactionDataResponse> retrieveReactionsFromOneComment(UUID commentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> data = commentReactionRepository.getReactionsFromOneComment(pageable, commentId);
        List<CommentReactionDataResponse> reactions = data.stream().map(row -> {
            UUID reactionId = (UUID) row[0];
            UUID userId = (UUID) row[1];
            Reactions reaction = (Reactions) row[2];
            String fullname = (String) row[3] + " " + row[4];
            String profilePicUrl = (String) row[5];
            return new CommentReactionDataResponse(reactionId, userId, reaction, fullname, profilePicUrl);
        })
                .collect(Collectors.toList());

        return reactions;
    }

    @Transactional
    public CommentReaction insertReaction(CreateCommentReactionRequest req) {
        CommentReaction commentReaction = new CommentReaction();
        Comment proxyComment = commentRepository.getReferenceById(req.getCommentId());
        User proxyUser = userRepository.getReferenceById(req.getUserId());
        commentReaction.setComment(proxyComment);
        commentReaction.setUser(proxyUser);
        commentReaction.setReaction(req.getReaction());

        CommentReaction newReaction = commentReactionRepository.save(commentReaction);
        commentRepository.incrementReactionCount(req.getCommentId());

        return newReaction;
    }
    
    @Transactional
    public CommentReaction changeReaction(UUID reactionId, CreateCommentReactionRequest req) {
        CommentReaction reaction = commentReactionRepository.findById(reactionId).orElseThrow(() -> {
            log.warn("User {} can't find comment reaction {} of comment {}", req.getUserId(), reactionId,
                    req.getCommentId());
            throw new CommentReactionException(CommentReactionErrorCode.REACTION_NOT_FOUND, "Can't find reaction");
        });

        if (!reaction.getUserId().equals(req.getUserId())) {
            log.warn("Comment reaction {} of comment {} isn't made by user {}", reactionId, req.getCommentId(),
                    req.getUserId());
            throw new CommentReactionException(CommentReactionErrorCode.REACTION_NOT_OWNED,
                    "Comment Reaction isn't made by user");
        }

        reaction.setReaction(req.getReaction());
        CommentReaction updatedReaction = commentReactionRepository.save(reaction);
        return updatedReaction;
    }
    
    public void removeReaction(UUID reactionId, UUID userId) {
        CommentReaction reaction = commentReactionRepository.findById(reactionId).orElseThrow(() -> {
            log.warn("User {} can't find comment reaction {}", userId, reactionId);
            throw new CommentReactionException(CommentReactionErrorCode.REACTION_NOT_FOUND, "Can't find reaction");
        });

        if (!reaction.getUserId().equals(userId)) {
            log.warn("Comment reaction {} isn't made by user {}", reactionId, userId);
            throw new CommentReactionException(CommentReactionErrorCode.REACTION_NOT_OWNED,
                    "Comment Reaction isn't made by user");
        }

        commentReactionRepository.delete(reaction);
        commentRepository.decrementReactionCount(reaction.getCommentId());
    }
}
