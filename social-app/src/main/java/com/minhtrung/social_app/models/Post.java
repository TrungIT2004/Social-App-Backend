package com.minhtrung.social_app.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Post {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID postId;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "interactableId", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_post_interactable"))
    @JsonIgnore
    private Interactable interactable;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "userId", referencedColumnName = "userId", foreignKey = @ForeignKey(name = "fk_post_user"))
    @JsonIgnore
    private User user;

    @ManyToMany
    @JoinTable(
        name = "post_hashtag",
        joinColumns = @JoinColumn(name = "postId"),
        inverseJoinColumns = @JoinColumn(name = "hashtagId")
    )
    @JsonIgnore
    private Set<Hashtag> hashtags = new HashSet<>();

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JsonIgnore
    private List<Share> shares = new ArrayList<>();

    @Column(nullable = true)
    private String text;

    @Column(nullable = true)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> mediaUrls = new ArrayList<>();

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updateddAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Visibility visibility = Visibility.PUBLIC;

    @Column(nullable = false)
    private int reactionCount = 0;

    @Column(nullable = false)
    private int commentCount = 0;

    @Column(nullable = false)
    private int shareCount = 0;

    public UUID getUserId() {
        return user != null ? user.getUserId() : null;
    }

    public UUID getInteractableId() {
        return interactable != null ? interactable.getId() : null;
    }

    @PrePersist
    public void prePersist() {
        if (this.interactable == null) {
            Interactable newInteractable = new Interactable();
            newInteractable.setType(InteractableType.post);
            this.interactable = newInteractable;
        }
    }
}

