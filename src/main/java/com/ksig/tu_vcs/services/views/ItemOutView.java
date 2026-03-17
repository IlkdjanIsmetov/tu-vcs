package com.ksig.tu_vcs.services.views;

import com.ksig.tu_vcs.repos.entities.enums.ItemType;
import java.util.UUID;

//правя го интерфейс защото искам спесифично тези данни да се вземат с една заявка към базата без да има нужда да правя нови @Repository и @Entity класове
public interface ItemOutView {
    UUID getId();
    String getPath();
    ItemType getItemType();
    UUID getRevisionId();
    Long getRevisionNumber();
    String getChecksum();
    UUID getStorageKey();
}