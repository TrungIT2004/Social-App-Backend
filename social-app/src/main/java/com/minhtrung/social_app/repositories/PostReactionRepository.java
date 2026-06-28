package com.minhtrung.social_app.repositories;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minhtrung.social_app.models.PostReaction;
import java.util.List;


public interface PostReactionRepository extends JpaRepository<PostReaction, UUID> {
    public List<PostReaction> findByinteractableId(UUID interactableId);

    @Query("""
        SELECT pr.reactionId, u.userId, pr.reaction, u.firstName, u.lastName, u.profilePicUrl
        FROM PostReaction pr
        JOIN pr.user u 
        WHERE pr.interactable.id = :interactableId
    """)
    Page<Object[]> getReactionsFromOnePost(Pageable pageable, @Param("interactableId") UUID interactableId);       
}
