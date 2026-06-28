package com.minhtrung.social_app.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.minhtrung.social_app.dtos.ReplyDataResponse;
import com.minhtrung.social_app.models.Comment;


public interface CommentRepository extends JpaRepository<Comment, UUID> {
    @Query("""
        SELECT c, u.firstName, u.lastName, u.profilePicUrl
        FROM Comment c
        JOIN c.user u 
        WHERE c.interactable.id = :interactableId
        AND c.comment is null        

    """)
    Page<Object[]> getCommentsFromOnePost(Pageable pageable, @Param("interactableId") UUID interactableId);


    @Query("""
        SELECT new com.minhtrung.social_app.dtos.ReplyDataResponse(c, CONCAT(u.firstName, ' ', u.lastName), u.profilePicUrl)
        FROM Comment c
        JOIN c.user u
        WHERE c.comment.commentId = :commentId
    """)
    List<ReplyDataResponse> getReplysFromOneComment(@Param("commentId") UUID commentId);

    @Modifying
    @Transactional
    @Query("""
            UPDATE Comment c
            SET c.reactionCount = c.reactionCount + 1
            WHERE c.commentId = :commentId
        """)
    public int incrementReactionCount(@Param("commentId") UUID commentId);

    @Modifying
    @Transactional
    @Query("""
            UPDATE Comment c
            SET c.reactionCount = c.reactionCount - 1
            WHERE c.commentId = :commentId
        """)
    int decrementReactionCount(@Param("commentId") UUID commentId);

    @Modifying
    @Transactional
    @Query("""
            UPDATE Comment c
            SET c.replyCount = c.replyCount + 1
            WHERE c.commentId = :commentId
        """)
    public int incrementReplyCount(@Param("commentId") UUID commentId);

    @Modifying
    @Transactional
    @Query("""
            UPDATE Comment c
            SET c.replyCount = c.replyCount - 1 
            WHERE c.commentId = :commentId
        """)
    public int decrementReplyCount(@Param("commentId") UUID commentId);
}
