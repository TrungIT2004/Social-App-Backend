package com.minhtrung.social_app.models;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import com.minhtrung.social_app.enums.FriendshipStatus;

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

@Entity
@Table(name = "friendships", uniqueConstraints = @UniqueConstraint(name = "uk_friendship", columnNames = {"requesterId", "addresseeId"}))
@Data
public class Friendship {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID friendshipId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name ="requesterId", referencedColumnName = "userId", nullable = false, foreignKey = @ForeignKey(name = "fk_friendship_requester"))
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name ="addresseeId", referencedColumnName = "userId", nullable = false, foreignKey = @ForeignKey(name = "fk_friendship_addressee"))
    private User addressee;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FriendshipStatus status = FriendshipStatus.PENDING;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
