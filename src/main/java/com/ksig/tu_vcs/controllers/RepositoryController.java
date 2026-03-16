package com.ksig.tu_vcs.controllers;

import com.ksig.tu_vcs.repos.entities.Repository;
import com.ksig.tu_vcs.services.RepositoryService;
import com.ksig.tu_vcs.services.views.ItemView;
import com.ksig.tu_vcs.services.views.RepositoryView;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/repositories")
public class RepositoryController {
    private final RepositoryService repositoryService;


    public RepositoryController(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @PostMapping
    public ResponseEntity<Repository> createRepository(@RequestBody RepositoryView view) {
        return ResponseEntity.ok(repositoryService.createRepository(view));
    }


    @PostMapping(value = "/{repositoryId}/commit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> commit(@PathVariable UUID repositoryId, @RequestPart("paths") List<ItemView> items,
                                         @RequestPart("files") List<MultipartFile> files, @RequestParam("message")  String message) {
        return ResponseEntity.ok(repositoryService.commitDirectly(repositoryId, items, files, message));
    }
}
