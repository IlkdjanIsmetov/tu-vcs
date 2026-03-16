package com.ksig.tu_vcs.repos.entities;

import com.ksig.tu_vcs.repos.entities.enums.SystemRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "app_user", schema = "vcs")
public class AppUser {

    @Id
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SystemRole systemRole;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}