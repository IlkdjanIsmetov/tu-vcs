package com.ksig.tu_vcs.services.views;

import com.ksig.tu_vcs.repos.entities.AppUser;
import com.ksig.tu_vcs.repos.entities.Repository;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryInViewTest {

    @Test
    void shouldMapRepositoryToInView() {

        AppUser owner = new AppUser();
        owner.setId(UUID.randomUUID());

        Repository repo = new Repository();
        repo.setName("name");
        repo.setDescription("desc");
        repo.setOwner(owner);
        repo.setRequiresApprovalByDefault(true);

        RepositoryInView result = RepositoryInView.fromEntity(repo);

        assertNotNull(result);
        assertEquals("name", result.getRepositoryName());
        assertEquals("desc", result.getDescription());
        assertEquals(owner.getId(), result.getOwnerId());
        assertTrue(result.isRequiresApprovalByDefault());
    }

}