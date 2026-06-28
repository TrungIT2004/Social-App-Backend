package com.minhtrung.social_app.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minhtrung.social_app.models.PendingUser;

public interface PendingUserRepository extends JpaRepository<PendingUser, UUID> {
    Optional<PendingUser> findByEmail(String email);

    boolean existsByEmail(String email);
    void deleteByEmail(String email);
}
