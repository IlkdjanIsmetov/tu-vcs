package com.ksig.tu_vcs.repos;

import com.ksig.tu_vcs.repos.entities.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepositoryRepository extends JpaRepository<Repository, UUID> {
    Optional<Repository> findByName(String name);
    Optional<Repository> findById(UUID id);
    List<Repository> findByOwnerId(UUID ownerId);
    List<Repository>findByNameContainingIgnoreCase(String name);
}
