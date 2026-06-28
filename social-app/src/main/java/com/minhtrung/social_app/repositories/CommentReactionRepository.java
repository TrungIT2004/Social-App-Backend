package com.minhtrung.social_app.repositories;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minhtrung.social_app.models.CommentReaction;

public interface CommentReactionRepository extends JpaRepository<CommentReaction, UUID> {
    @Query("""
        SELECT cr.reactionId, u.userId, cr.reaction, u.firstName, u.lastName, u.profilePicUrl
        FROM CommentReaction cr
        JOIN cr.user u
        WHERE cr.comment.commentId = :commentId              
    """)
    Page<Object[]> getReactionsFromOneComment(Pageable pageable, @Param("commentId") UUID commentId);
}
