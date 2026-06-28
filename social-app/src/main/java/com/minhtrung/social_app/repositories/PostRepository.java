package com.minhtrung.social_app.repositories;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.minhtrung.social_app.models.Post;

public interface PostRepository extends JpaRepository<Post, UUID> {
    @Query("""
        SELECT p, u.firstName, u.lastName, u.profilePicUrl
        FROM Post p
        JOIN p.user u    
    """)
    Page<Object[]> getPosts(Pageable pageable);
    
    @Query("""
        SELECT p, u.firstName, u.lastName, u.profilePicUrl
        FROM Post p
        JOIN p.user u
        JOIN p.hashtags h
        WHERE h.hashtagName = :hashtagName 
    """)
    Page<Object[]> getPostsFromHashtag(Pageable pageable, @Param("hashtagName") String hashtagName);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.reactionCount = p.reactionCount + 1 WHERE p.interactable.id = :interactableId")
    int increaseReactionCount(@Param("interactableId") UUID interactableId);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.reactionCount = p.reactionCount - 1 WHERE p.interactable.id = :interactableId")
    int decreaseReactionCount(@Param("interactableId") UUID interactableId);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.interactable.id = :interactableId")
    int incrementCommentCount(@Param("interactableId") UUID interactableId);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.commentCount = p.commentCount - 1 WHERE p.interactable.id = :interactableid")
    int decrementCommentCount(@Param("interactableId") UUID interactableId);
}








