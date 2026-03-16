package com.ksig.tu_vcs.repos;

import com.ksig.tu_vcs.repos.entities.ItemRevision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ItemRevisionRepository extends JpaRepository<ItemRevision, UUID> {
    List<ItemRevision> findByRevisionId(UUID revisionId);
    List<ItemRevision> findByItemId(UUID itemId);
}