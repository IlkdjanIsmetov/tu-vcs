package com.ksig.tu_vcs.services.views;

import com.ksig.tu_vcs.repos.entities.Repository;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class RepositoryInView {
    private String repositoryName;
    private String description;
    private UUID ownerId;
    private boolean requiresApprovalByDefault;

    public static RepositoryInView fromEntity(Repository entity) {
        RepositoryInView view = new RepositoryInView();
        view.repositoryName = entity.getName();
        view.description = entity.getDescription();
        view.ownerId = entity.getOwner().getId();
        view.requiresApprovalByDefault = entity.getRequiresApprovalByDefault();
        return view;
    }
}