package com.minhtrung.social_app.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minhtrung.social_app.dtos.ShareDataResponse;
import com.minhtrung.social_app.models.Share;

public interface ShareRepository extends JpaRepository<Share, UUID> {
    @Query(
        """
            SELECT s, p.text, p.mediaUrls, p.createdAt, u.firstName, u.lastName, u.profilePicUrl
            FROM Share s
            JOIN s.post p
            JOIN s.user u
            WHERE u.userId = :userId
        """)
    List<Object[]> retrieveAllSharedPostsFromOneUser(@Param("userId") UUID userId);
    
    @Query("""
        SELECT new com.minhtrung.social_app.dtos.ShareDataResponse(
            s, CONCAT(u.firstName, ' ', u.lastName), u.profilePicUrl, p.text, p.mediaUrls, p.createdAt,
            CONCAT(owner.firstName, ' ', owner.lastName), owner.profilePicUrl
        )
        FROM Share s
        JOIN s.post p
        JOIN s.user u
        JOIN p.user owner
        WHERE s.shareId = :shareId
        """)
    Optional<ShareDataResponse> retrieveOneSharedPost(@Param("shareId") UUID shareId);

    @Modifying
    @Query("""
        UPDATE Share s
        SET s.reactionCount = s.reactionCount + 1
        WHERE s.interactable.id = :interactableId
    """)
    int incrementReactionCount(@Param("interactableId") UUID interactableId);

    @Modifying
    @Query("""
        UPDATE Share s
        SET s.reactionCount = s.reactionCount - 1
        WHERE s.interactable.id = :interactableId
    """)
    int decrementReactionCount(@Param("interactableId") UUID interactableId);


    @Modifying
    @Query("""
        UPDATE Share s
        SET s.commentCount = s.commentCount + 1
        WHERE s.interactable.id = :interactableId
    """)
    int incrementCommentCount(@Param("interactableId") UUID interactableId);

    @Modifying
    @Query("""
        UPDATE Share s
        SET s.commentCount = s.commentCount - 1
        WHERE s.interactable.id = :interactableId
    """)
    int decrementCommentCount(@Param("interactableId") UUID interactableId);
}
