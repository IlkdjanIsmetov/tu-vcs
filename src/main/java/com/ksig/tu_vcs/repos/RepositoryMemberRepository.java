package com.ksig.tu_vcs.repos;

import com.ksig.tu_vcs.repos.entities.RepositoryMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RepositoryMemberRepository extends JpaRepository<RepositoryMember, UUID> {
    Optional<RepositoryMember> findByRepositoryIdAndUserId(UUID repositoryId, UUID userId);
}
