package com.ksig.tu_vcs.services.views;

import com.ksig.tu_vcs.repos.entities.enums.Action;
import com.ksig.tu_vcs.repos.entities.enums.ItemType;
import lombok.Data;

@Data
public class ItemView {
    private String path;
    private ItemType itemType;
    private Action action;
    private int fileSize;
    private String checksum;
    private String fileRef;
}
