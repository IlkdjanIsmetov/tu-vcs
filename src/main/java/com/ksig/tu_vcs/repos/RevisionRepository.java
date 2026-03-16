package com.ksig.tu_vcs.repos;

import com.ksig.tu_vcs.repos.entities.Revision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RevisionRepository extends JpaRepository<Revision, UUID> {
    Optional<Revision> findByRepositoryIdAndRevisionNumber(UUID repositoryId, Long revisionNumber);
    List<Revision> findByRepositoryIdOrderByRevisionNumberDesc(UUID repositoryId);
}