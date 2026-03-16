package com.ksig.tu_vcs.repos.entities;


import com.ksig.tu_vcs.repos.entities.enums.ItemType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Data
@Entity
@Table(name = "item", schema = "vcs", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"repository_id", "path"})
})
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repository_id", nullable = false)
    private Repository repository;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String path;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    private ItemType itemType;
}