package com.ksig.tu_vcs.controllers;

import com.ksig.tu_vcs.services.PullService;
import com.ksig.tu_vcs.services.views.LocalItemMetadata;
import com.ksig.tu_vcs.services.views.SyncItemView;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ksig.tu_vcs.services.exceptions.ResourceNotFoundException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/repositories")
public class PullController {
    private final PullService pullService;

    public PullController(PullService pullService) {
        this.pullService = pullService;
    }

    @PostMapping("/{repositoryId}/sync-status")
    public ResponseEntity<List<SyncItemView>> getSyncStatus(@PathVariable("repositoryId") UUID repositoryId,
                                                            @RequestAttribute List<LocalItemMetadata> localManifest,
                                                            HttpServletRequest request){
        String logId = UUID.randomUUID().toString();
        request.setAttribute("logId", logId);
        return ResponseEntity.ok( pullService.checkSyncStatus(repositoryId,localManifest,logId));
    }

    @GetMapping("/{repositoryId}/content/{storageKey}")
    public ResponseEntity<Resource> downloadFile(@PathVariable UUID repositoryId,
                                                 @PathVariable String storageKey,
                                                 HttpServletRequest request){
        String logId = UUID.randomUUID().toString();
        request.setAttribute("logId", logId);
        Resource resource = pullService.pullFileContent(repositoryId,storageKey,logId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + storageKey + "\"")
                .body(resource);
    }


    @GetMapping("/{repositoryId}/content")
    public ResponseEntity<String> getFileContent(@PathVariable UUID repositoryId,
                                                 @RequestParam("path") String filePath,
                                                 @RequestParam(value = "rev", required = false) Long revision,
                                                 HttpServletRequest request) throws IOException {
        String logId = UUID.randomUUID().toString();
        request.setAttribute("logId", logId);
        String storageKey = pullService.getStorageKey(repositoryId,filePath,revision,logId);

        return ResponseEntity.ok(pullService.loadFileContent(storageKey,logId));

    }
}
