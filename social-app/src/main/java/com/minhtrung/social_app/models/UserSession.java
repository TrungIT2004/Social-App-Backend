package com.minhtrung.social_app.models;

import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "user_session")
public class UserSession {
    @Id
    private UUID sessionId;
    
    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID deviceId;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String token;

    @Column(nullable = false)
    private String deviceType;

    @Column(nullable = false)
    private String deviceOS;

    @Column(nullable = false)
    private String browser;
    
    @Column(nullable = false)
    private String ipAdd;

    @Column(nullable = false)
    private String country;
    
    @Column(nullable = false)
    private String city;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean revoked = false;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDate createdAt;
}
