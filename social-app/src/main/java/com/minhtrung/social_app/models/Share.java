package com.minhtrung.social_app.models;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.minhtrung.social_app.enums.InteractableType;
import com.minhtrung.social_app.enums.Visibility;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import lombok.Data;

@Data
@Entity
public class Share {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID shareId;

    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "interactableId", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_share_interactable"))
    @JsonIgnore
    private Interactable interactable;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "postId", referencedColumnName = "postId", foreignKey = @ForeignKey(name = "fk_share_post"))
    @JsonIgnore
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "userId", referencedColumnName = "userId", foreignKey = @ForeignKey(name = "fk_share_user"))
    @JsonIgnore
    private User user;

    @Column(nullable = true)
    private String textContent;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Visibility visibility = Visibility.PUBLIC;

    @Column(nullable = false)
    private int reactionCount = 0;

    @Column(nullable = false)
    private int commentCount = 0;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    public UUID getUserId() {
        return user != null ? user.getUserId() : null;
    }

    public UUID getPostId() {
        return post != null ? post.getPostId() : null;
    }

    public UUID getInteractableId() {
        return interactable != null ? interactable.getId() : null;
    }

    @PrePersist
    public void prePersist() {
        if (this.interactable == null) {
            Interactable newInteractable = new Interactable();
            newInteractable.setType(InteractableType.share);
            this.interactable = newInteractable;
        }
    }
}
