package com.minhtrung.social_app.models;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Hashtag {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID hashtagId;
    
    @Column(nullable = false, unique = true)
    private String hashtagName;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

}
