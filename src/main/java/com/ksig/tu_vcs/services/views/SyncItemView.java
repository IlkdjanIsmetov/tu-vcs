package com.ksig.tu_vcs.services.views;

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
    private com.ksig.tu_vcs.services.views.SyncStatus status;
    private String serverChecksum;
    private String storageKey;
    private Long serverRevisionNumber;
}