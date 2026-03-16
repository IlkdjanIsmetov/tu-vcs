package com.ksig.tu_vcs.controllers;

import com.ksig.tu_vcs.repos.entities.Repository;
import com.ksig.tu_vcs.services.RepositoryService;
import com.ksig.tu_vcs.services.views.RepositoryView;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/repositories")
public class RepositoryController {
    private final RepositoryService repositoryService;


    public RepositoryController(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @PostMapping
    public Repository createRepository(@RequestBody RepositoryView view) {
        return repositoryService.createRepository(view);
    }


}
