package com.ksig.tu_vcs.repos;

import com.ksig.tu_vcs.repos.entities.Revision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RevisionRepository extends JpaRepository<Revision, UUID> {
    Optional<Revision> findByRepositoryIdAndRevisionNumber(UUID repositoryId, Long revisionNumber);
    List<Revision> findByRepositoryIdOrderByRevisionNumberDesc(UUID repositoryId);
    @Query("""
        SELECT r FROM Revision r 
        WHERE r.repository.id = :repositoryId 
          AND r.revisionNumber = (SELECT MAX(r2.revisionNumber) FROM Revision r2 WHERE r2.repository.id = :repositoryId)
    """)
    Optional<Revision> findLatestRevision(@Param("repositoryId") UUID repositoryId);
}