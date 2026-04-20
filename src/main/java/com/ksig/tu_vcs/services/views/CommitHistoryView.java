package com.ksig.tu_vcs.services.views;


import com.ksig.tu_vcs.repos.entities.Revision;
import lombok.Data;

import java.time.Instant;


@Data
public class CommitHistoryView {
    private Long revisionNumber;
    private String username;
    private String message;
    private Instant createdAt;

    public static CommitHistoryView fromRevision(Revision revision) {
        CommitHistoryView commitHistoryView = new CommitHistoryView();
        commitHistoryView.setRevisionNumber(revision.getRevisionNumber());
        commitHistoryView.setUsername(revision.getAuthor().getUsername());
        commitHistoryView.setMessage(revision.getMessage());
        commitHistoryView.setCreatedAt(revision.getCreatedAt());
        return commitHistoryView;
    }
}
