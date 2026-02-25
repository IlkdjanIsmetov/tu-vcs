package com.ksig.tu_vcs.repos.entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "commit_record")
public class CommitRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String repositoryName;
    private String commitHash;
    private String author;
    private boolean vulnerabilityFound;
}