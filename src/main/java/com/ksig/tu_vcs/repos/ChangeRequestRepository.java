package com.ksig.tu_vcs.repos;

import com.ksig.tu_vcs.repos.entities.ChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChangeRequestRepository extends JpaRepository<ChangeRequest, UUID> {
    List<ChangeRequest> findByRepositoryId(UUID repositoryId);
    List<ChangeRequest> findByAuthorId(UUID authorId);
}