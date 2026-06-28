package com.minhtrung.social_app.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Entity
@Data
public class Comment {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID commentId;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interactableId", referencedColumnName = "id", nullable = false, foreignKey = @ForeignKey(name = "fk_comment_interatable"))
    @JsonIgnore
    private Interactable interactable;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "userId", referencedColumnName = "userId", foreignKey = @ForeignKey(name = "fk_comment_user"))
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "comment", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CommentReaction> reactions = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parentCommentId", referencedColumnName = "commentId", foreignKey = @ForeignKey(name = "fk_comment_reply"))
    @JsonIgnore
    private Comment comment;

    @OneToMany(mappedBy = "comment", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Comment> replys = new ArrayList<>();

    @Column(nullable = true) 
    private String textContent;

    @Column(nullable = true)
    private String mediaUrl;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private int reactionCount = 0;

    @Column(nullable = false)
    private int replyCount = 0;

    public UUID getInteractableId() {
        return interactable != null ? interactable.getId() : null;
    }

    public UUID getParentCommentId() {
        return comment != null ? comment.getCommentId() : null;
    }

    public UUID getUserId() {
        return user != null ? user.getUserId() : null;
    }
}
