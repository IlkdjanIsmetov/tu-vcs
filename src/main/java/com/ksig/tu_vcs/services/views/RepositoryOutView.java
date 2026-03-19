package com.ksig.tu_vcs.services.views;

import com.ksig.tu_vcs.repos.entities.Repository;
import lombok.Data;

import java.util.UUID;

@Data
public class RepositoryOutView {
    private UUID id;
    private String name;
    private String description;
    private boolean requireApproval;
    private Long revision;
    private String url;

    public static RepositoryOutView fromEntity(Repository entity) {
        if (entity == null) {
            return null;
        }

        RepositoryOutView view = new RepositoryOutView();
        view.setId(entity.getId());
        view.setName(entity.getName());
        view.setDescription(entity.getDescription());
        view.setRequireApproval(entity.getRequiresApprovalByDefault());
        return view;
    }
}