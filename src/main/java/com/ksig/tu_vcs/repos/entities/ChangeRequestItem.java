package com.ksig.tu_vcs.repos.entities;


import com.ksig.tu_vcs.repos.entities.enums.Action;
import com.ksig.tu_vcs.repos.entities.enums.ItemType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Data
@Entity
@Table(name = "change_request_item", schema = "vcs")
public class ChangeRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "change_request_id", nullable = false)
    private ChangeRequest changeRequest;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String path;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    private ItemType itemType;

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