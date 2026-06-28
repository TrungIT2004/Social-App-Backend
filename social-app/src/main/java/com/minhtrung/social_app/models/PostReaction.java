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
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_user_interactable", columnNames = {"userId", "interactableId"}))
public class PostReaction {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID reactionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interactableId", referencedColumnName = "id", nullable = false, foreignKey = @ForeignKey(name = "fk_post_reaction_interatable"))
    @JsonIgnore
    private Interactable interactable;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "userId", referencedColumnName = "userId", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_post_reaction_user"))
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

    public UUID getInteractableId() {
        return interactable != null ? interactable.getId() : null;
    }

    public UUID getUserId() {
        return user != null ? user.getUserId() : null;
    }
}