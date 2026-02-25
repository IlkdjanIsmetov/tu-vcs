package com.ksig.tu_vcs.services;


import com.ksig.tu_vcs.repos.CommitRecordRepository;
import com.ksig.tu_vcs.repos.entities.CommitRecord;
import com.ksig.tu_vcs.services.views.CommitRecordView;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommitRecordService {

    private final CommitRecordRepository repository;

    public CommitRecordService(CommitRecordRepository repository) {
        this.repository = repository;
    }

        public List<CommitRecordView> getAllCommitViews() {
        return repository.findAll()
                .stream()
                .map(CommitRecordView::fromEntity)
                .collect(Collectors.toList());
    }
}