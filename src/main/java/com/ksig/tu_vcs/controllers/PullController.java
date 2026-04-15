package com.ksig.tu_vcs.controllers;

import com.ksig.tu_vcs.services.PullService;
import com.ksig.tu_vcs.services.views.LocalItemMetadata;
import com.ksig.tu_vcs.services.views.SyncItemView;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pull")
public class PullController {
    private final PullService pullService;

    public PullController(PullService pullService) {
        this.pullService = pullService;
    }

    @PostMapping("/{repositoryId}/sync-status")
    public ResponseEntity<List<SyncItemView>> getSyncStatus(@PathVariable("repositoryId") UUID repositoryId,
                                                      @RequestAttribute List<LocalItemMetadata> localManifest){
        //add logId
        return ResponseEntity.ok( pullService.checkSyncStatus(repositoryId,localManifest));
    }

    public ResponseEntity<Resource> downloadFile(@PathVariable UUID repositoryId,
                                                 @PathVariable String storageKey){
        Resource resource = pullService.pullFileContent(repositoryId,storageKey);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + storageKey + "\"")
                .body(resource);
    }
}
