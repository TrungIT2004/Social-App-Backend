package com.minhtrung.social_app.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.minhtrung.social_app.models.User;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.email = :email")
    User findByEmail(String email);
}
