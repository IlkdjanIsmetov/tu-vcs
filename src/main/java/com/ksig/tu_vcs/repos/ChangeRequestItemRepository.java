package com.ksig.tu_vcs.repos;

import com.ksig.tu_vcs.repos.entities.ChangeRequestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChangeRequestItemRepository extends JpaRepository<ChangeRequestItem, UUID> {
    List<ChangeRequestItem> findByChangeRequestId(UUID changeRequestId);
}