package com.minhtrung.social_app.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minhtrung.social_app.models.Interactable;


public interface InteractableRepository extends JpaRepository<Interactable, UUID> {
    Optional<Interactable> findById(UUID id);
}
