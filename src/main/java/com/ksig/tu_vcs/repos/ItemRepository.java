package com.ksig.tu_vcs.repos;

import com.ksig.tu_vcs.repos.entities.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {
    Optional<Item> findByRepositoryIdAndPath(UUID repositoryId, String path);
    List<Item> findByRepositoryId(UUID repositoryId);
}