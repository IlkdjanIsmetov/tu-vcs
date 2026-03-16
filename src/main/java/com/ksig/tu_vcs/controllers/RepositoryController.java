package com.ksig.tu_vcs.controllers;

import com.ksig.tu_vcs.repos.entities.Repository;
import com.ksig.tu_vcs.services.RepositoryService;
import com.ksig.tu_vcs.services.views.RepositoryView;
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


    @PostMapping("/{repositoryId}/commit")
    public ResponseEntity<String> commit(@PathVariable UUID repositoryId, @RequestParam("paths") List<String> paths,
                                         @RequestParam("files") List<MultipartFile> files, @RequestParam("message")  String message) {
        return ResponseEntity.ok(repositoryService.commitDirectly(repositoryId, paths, files, message));
    }
}
