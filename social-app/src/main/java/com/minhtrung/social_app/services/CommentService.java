package com.minhtrung.social_app.services;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.minhtrung.social_app.dtos.CommentDataResponse;
import com.minhtrung.social_app.dtos.CreateCommentRequest;
import com.minhtrung.social_app.dtos.ReplyDataResponse;
import com.minhtrung.social_app.enums.CommentErrorCode;
import com.minhtrung.social_app.enums.InteractableType;
import com.minhtrung.social_app.exceptions.CommentException;
import com.minhtrung.social_app.models.Comment;
import com.minhtrung.social_app.models.Interactable;
import com.minhtrung.social_app.models.User;
import com.minhtrung.social_app.repositories.CommentRepository;
import com.minhtrung.social_app.repositories.InteractableRepository;
import com.minhtrung.social_app.repositories.PostRepository;
import com.minhtrung.social_app.repositories.ShareRepository;
import com.minhtrung.social_app.repositories.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ShareRepository shareRepository;
    private final InteractableRepository interactableRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    CommentService(CommentRepository commentRepository, InteractableRepository interactableRepository, PostRepository postRepository, ShareRepository shareRepository, UserRepository userRepository, FileStorageService fileStorageService) {
        this.commentRepository = commentRepository;
        this.interactableRepository = interactableRepository;
        this.postRepository = postRepository;
        this.shareRepository = shareRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }
    
    public List<CommentDataResponse> retrieveCommentsFromOnePost(int page, int size, UUID interactableId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> data = commentRepository.getCommentsFromOnePost(pageable, interactableId);
        List<CommentDataResponse> res = data.stream()
                .map(row -> {
                    Comment comment = (Comment) row[0];
                    String fullname = (String) row[1] + " " + row[2];
                    String profilePicUrl = (String) row[3];
                    return new CommentDataResponse(comment, fullname, profilePicUrl);
                })
                .collect(Collectors.toList());

        return res;
    }
    
    // Remember to check if that comment exists later, Im too lazy rn
    public List<ReplyDataResponse> retrieveAllReplysForOneComment(UUID commentId) {
        List<ReplyDataResponse> replys = commentRepository.getReplysFromOneComment(commentId);
        return replys;
    }

    @Transactional
    public Comment insertComment(CreateCommentRequest req, UUID userId, MultipartFile file) {
        Comment newComment = new Comment();

        String text = req.getTextContent();
        UUID interactableId = req.getInteractableId();
        UUID parentCommentId = req.getParentCommentId();

        boolean hasText = text != null && !text.isBlank();
        boolean hasFile = file != null && !file.isEmpty();
        boolean isReply = parentCommentId != null;

        if (hasText) {
            newComment.setTextContent(req.getTextContent());
        } else {
            newComment.setTextContent(null);
        }

        if (hasFile) {
            try {
                String fileName = fileStorageService.storeFile(file, "comments");
                newComment.setMediaUrl(fileName);
            } catch (IOException ex) {
                throw new RuntimeException("Can't upload file", ex);
            }
        } else {
            newComment.setMediaUrl(null);
        }

        if (!hasText && !hasFile) {
            log.warn("User {} tried to post empty conment", userId);
            throw new CommentException(CommentErrorCode.NO_CONTENT_ERROR, "A comment has to have contents");
        }

        User proxyUser = userRepository.getReferenceById(userId);
        Interactable proxyInteractable = interactableRepository.getReferenceById(interactableId);
        newComment.setUser(proxyUser);
        newComment.setInteractable(proxyInteractable);
        newComment.setComment(null);

        if (isReply) {
            Comment commentProxy = commentRepository.getReferenceById(parentCommentId);
            newComment.setComment(commentProxy);
            commentRepository.incrementReplyCount(parentCommentId);
        } else {
            Interactable interactable = interactableRepository.findById(interactableId).orElseThrow( () -> {
                log.warn("Can't find interactable {}", interactableId);
                throw new RuntimeException("Can't find interactable");
            });

            boolean isPost = interactable.getType() == InteractableType.post;
            boolean isSharedPost = interactable.getType() == InteractableType.share;

            if (isPost) {
                postRepository.incrementCommentCount(interactableId);
            } else if (isSharedPost) {
                shareRepository.incrementCommentCount(interactableId);
            }
        }

        Comment createdComment = commentRepository.save(newComment);
        log.info("User {} commented a comment {} on post {}", userId, createdComment.getCommentId(), interactableId);
        log.debug("Save a new comment in DB: {}", createdComment);

        return createdComment;
    }
    
    @Transactional
    public void removeComment(UUID commentId, UUID userId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow( () -> {
            log.warn("Can't find comment {}", commentId);
            throw new CommentException(CommentErrorCode.COMMENT_NOT_FOUND, "Can't find comment {}");
        });

        if (!comment.getUserId().equals(userId)) {
            log.warn("Comment {} doesn't belong to user {}", commentId, userId);
            throw new CommentException(CommentErrorCode.COMMENT_NOT_OWNED, "Comment doesn't belong to user");
        }

        UUID parentCommentId = comment.getParentCommentId();
        UUID interactableId = comment.getInteractableId();
        boolean isReply = parentCommentId != null ? true : false;

        if (isReply) {
            commentRepository.decrementReplyCount(parentCommentId);
        } else {
            Interactable interactable = interactableRepository.findById(interactableId).orElseThrow( () -> {
                log.warn("Can't find interactable {}", interactableId);
                throw new RuntimeException("Can't find interactable");
            });

            boolean isPost = interactable.getType() == InteractableType.post;
            boolean isSharedPost = interactable.getType() == InteractableType.share;

            if (isPost) {
                postRepository.decrementCommentCount(interactableId);
            } else if (isSharedPost) {
                shareRepository.decrementCommentCount(interactableId);
            }
        }

        commentRepository.delete(comment);
    }
}
