package com.ksig.tu_vcs.repos.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "repository_member", schema = "vcs")
public class RepositoryMember {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "repository_id")
    private UUID repositoryId;

    @Column(name = "user_id")
    private UUID userID;

    private String role;
}
