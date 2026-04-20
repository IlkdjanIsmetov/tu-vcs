package com.ksig.tu_vcs.services.views;

import com.ksig.tu_vcs.repos.entities.ChangeRequest;
import lombok.Data;

import java.util.UUID;

@Data
public class ChangeRequestOutView {
    private UUID id;
    private String title;
    private String description;
    private String status;
    private Long baseRevisionNumber;
    private String authorName;
    private String createdAt;

    public static ChangeRequestOutView fromEntity(ChangeRequest entity){
        ChangeRequestOutView changeRequestOutView = new ChangeRequestOutView();
        changeRequestOutView.setId(entity.getId());
        changeRequestOutView.setTitle(entity.getTitle());
        changeRequestOutView.setDescription(entity.getDescription());
        changeRequestOutView.setStatus(entity.getStatus().toString());
        changeRequestOutView.setBaseRevisionNumber(entity.getBaseRevisionNumber());
        changeRequestOutView.setAuthorName(entity.getAuthor().getUsername());
        changeRequestOutView.setCreatedAt(entity.getCreatedAt().toString());

        return changeRequestOutView;
    }

}
