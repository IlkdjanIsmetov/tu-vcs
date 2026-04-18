package com.ksig.tu_vcs.repos.entities;

import com.ksig.tu_vcs.repos.entities.enums.Role;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryMemberTest {
    @Test
    void shouldAllowCommitForMaster() {

        RepositoryMember member = new RepositoryMember();
        member.setRole(Role.MASTER);

        assertTrue(member.canCommit());
    }

    @Test
    void shouldAllowCommitForContributor() {

        RepositoryMember member = new RepositoryMember();
        member.setRole(Role.CONTRIBUTOR);

        assertTrue(member.canCommit());
    }

    @Test
    void shouldNotAllowCommitForViewer() {

        RepositoryMember member = new RepositoryMember();
        member.setRole(Role.VIEWER);

        assertFalse(member.canCommit());
    }

    @Test
    void shouldThrowWhenRoleIsNull() {

        RepositoryMember member = new RepositoryMember();

        assertThrows(NullPointerException.class, member::canCommit);
    }
}