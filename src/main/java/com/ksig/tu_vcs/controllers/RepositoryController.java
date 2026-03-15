package com.ksig.tu_vcs.controllers;

import com.ksig.tu_vcs.repos.dto.RepositoryRequest;
import com.ksig.tu_vcs.repos.entities.Repository;
import com.ksig.tu_vcs.services.RepositoryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/repositories")
public class RepositoryController {
    private final RepositoryService repositoryService;


    public RepositoryController(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @PostMapping
    public Repository createRepository(@RequestBody RepositoryRequest request) {
        return repositoryService.createRepository(
                request.name(),
                request.description(),
                request.ownerId()
        );
    }
}
