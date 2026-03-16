package com.ksig.tu_vcs.services.views;

import com.ksig.tu_vcs.repos.entities.Repository;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class RepositoryView {
    private String repositoryName;
    private String description;
    private UUID ownerId;
    private boolean requiresApprovalByDefault;

    public static RepositoryView fromEntity(Repository entity) {
        RepositoryView view = new RepositoryView();
        view.repositoryName = entity.getName();
        view.description = entity.getDescription();
        view.ownerId = entity.getOwner().getId();
        view.requiresApprovalByDefault = entity.getRequiresApprovalByDefault();
        return view;
    }
}
