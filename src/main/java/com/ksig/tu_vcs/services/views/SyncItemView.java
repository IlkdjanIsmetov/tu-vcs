package com.ksig.tu_vcs.services.views;

import com.ksig.tu_vcs.repos.entities.enums.ItemType;
import com.ksig.tu_vcs.repos.entities.enums.SyncStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncItemView {
    private UUID itemId;
    private String path;
    private SyncStatus status;
    private String serverChecksum;
    private String storageKey;
    private Long serverRevisionNumber;
    private ItemType itemType;
}