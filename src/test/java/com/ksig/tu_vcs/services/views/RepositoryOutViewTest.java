package com.ksig.tu_vcs.services.views;

import com.ksig.tu_vcs.repos.entities.Repository;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryOutViewTest {

    @Test
    void shouldMapRepositoryToView() {

        Repository repo = new Repository();
        repo.setId(UUID.randomUUID());
        repo.setName("name");
        repo.setDescription("desc");
        repo.setRequiresApprovalByDefault(true);

        RepositoryOutView result = RepositoryOutView.fromEntity(repo);

        assertNotNull(result);
        assertEquals(repo.getId(), result.getId());
        assertEquals("name", result.getName());
        assertEquals("desc", result.getDescription());
        assertTrue(result.isRequireApproval());
    }

    @Test
    void shouldReturnNullWhenEntityIsNull() {

        RepositoryOutView result = RepositoryOutView.fromEntity(null);

        assertNull(result);
    }
}