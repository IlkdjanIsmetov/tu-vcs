package com.ksig.tu_vcs.repos.entities;


import com.ksig.tu_vcs.repos.entities.enums.Action;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Data
@Entity
@Table(name = "item_revision", schema = "vcs")
public class ItemRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "revision_id", nullable = false)
    private Revision revision;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Action action;

    @Column(name = "storage_key")
    private String storageKey;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(length = 64)
    private String checksum; 
}