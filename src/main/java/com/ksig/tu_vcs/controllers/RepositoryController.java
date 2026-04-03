package com.ksig.tu_vcs.controllers;

import com.ksig.tu_vcs.repos.entities.Repository;
import com.ksig.tu_vcs.repos.entities.enums.Role;
import com.ksig.tu_vcs.services.RepositoryService;
import com.ksig.tu_vcs.services.views.ItemInView;
import com.ksig.tu_vcs.services.views.ItemOutView;
import com.ksig.tu_vcs.services.views.RepositoryInView;
import com.ksig.tu_vcs.services.views.RepositoryOutView;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/repositories")
public class RepositoryController {
    private final RepositoryService repositoryService;


    public RepositoryController(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @PostMapping("/create")
    public ResponseEntity<RepositoryOutView> createRepository(@RequestBody RepositoryInView view) {
        RepositoryOutView out = repositoryService.createRepository(view);
        String repoUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/repositories/" + out.getId())
                .toUriString();
        out.setUrl(repoUrl);
        return ResponseEntity.ok(out);
    }

    @DeleteMapping("/{repositoryId}")
    public ResponseEntity<String> deleteRepository(@PathVariable("repositoryId") UUID repositoryId) {
        repositoryService.deleteRepository(repositoryId);
        return ResponseEntity.ok("OK");
    }


    @PostMapping(path = "/{repositoryId}/commit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> commit(@PathVariable("repositoryId") UUID repositoryId, @RequestPart("paths") List<ItemInView> items,
                                         @RequestPart("files") List<MultipartFile> files, @RequestParam("message") String message) {
        return ResponseEntity.ok(repositoryService.commitDirectly(repositoryId, items, files, message));
    }

    @GetMapping("/{repositoryId}/fetch")
    public ResponseEntity<List<ItemOutView>> fetchItmes(@PathVariable("repositoryId") UUID repositoryId) {
        return ResponseEntity.ok(repositoryService.fetchLatestRevision(repositoryId));
    }

    @PostMapping("/{repositoryId}/addMember")
    public ResponseEntity<String> addMember(@PathVariable("repositoryId") UUID repositoryId, @RequestParam("username") String username,
                                       @RequestParam("role")
                                       Role role) {
        repositoryService.addMember(repositoryId, username, role);
        return ResponseEntity.ok("OK");
    }

    @DeleteMapping("/{repositoryId}/kickMember")
    public ResponseEntity<String> kickMember(@PathVariable("repositoryId") UUID repositoryId, @RequestParam("username") String username) {
        repositoryService.kickMember(repositoryId, username);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/{repositoryId}/clone")
    public ResponseEntity<Resource> downloadZippedRepo(@RequestParam("repositoryId") UUID repositoryId) throws IOException {
        Path zipFilePath = repositoryService.getZippedRepo(repositoryId);
        Resource fileResource = new FileSystemResource(zipFilePath);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileResource.getFilename() + "\"")
                .contentType(MediaType.valueOf("application/zip"))
                .contentLength(fileResource.contentLength())
                .body(fileResource);
    }
}
