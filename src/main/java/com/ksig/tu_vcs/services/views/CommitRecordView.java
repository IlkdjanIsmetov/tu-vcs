package com.ksig.tu_vcs.services.views;


import com.ksig.tu_vcs.repos.entities.CommitRecord;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Getter
@NoArgsConstructor()
public class CommitRecordView {
    private String repositoryName;
    private String commitHash;
    private boolean isSecure;


    public static CommitRecordView fromEntity(CommitRecord entity) {
        CommitRecordView view = new CommitRecordView();
        view.repositoryName = entity.getRepositoryName();
        view.commitHash = entity.getCommitHash();
        view.isSecure = !entity.isVulnerabilityFound();
        return view;
    }
}