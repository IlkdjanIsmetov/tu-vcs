package com.ksig.tu_vcs.controllers;


import com.ksig.tu_vcs.services.CommitRecordService;
import com.ksig.tu_vcs.services.views.CommitRecordView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CommitRecordController {

    private final CommitRecordService service;

    public CommitRecordController(CommitRecordService service) {
        this.service = service;
    }

    @GetMapping("/commits")
    public ResponseEntity<List<CommitRecordView>> getCommits() {
        return ResponseEntity.ok(service.getAllCommitViews());
    }
}