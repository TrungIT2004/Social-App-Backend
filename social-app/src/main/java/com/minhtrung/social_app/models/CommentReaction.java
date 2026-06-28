package com.minhtrung.social_app.models;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.minhtrung.social_app.enums.Reactions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Data
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_user_comment", columnNames = { "userId", "commentId" }))
@Entity
public class CommentReaction {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID reactionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "commentId", referencedColumnName = "commentId", foreignKey = @ForeignKey(name = "fk_comment_reaction_comment"))
    @JsonIgnore
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "userId", referencedColumnName = "userId", foreignKey = @ForeignKey(name = "fk_comment_reaction_user"))
    @JsonIgnore
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Reactions reaction;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public UUID getCommentId() {
        return comment != null ? comment.getCommentId() : null;
    }
    
    public UUID getUserId() {
        return user != null ? user.getUserId() : null;
    }
}
