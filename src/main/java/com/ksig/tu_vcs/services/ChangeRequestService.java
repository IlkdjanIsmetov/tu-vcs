package com.ksig.tu_vcs.services;

import com.ksig.tu_vcs.repos.AppUserRepository;
import com.ksig.tu_vcs.repos.ChangeRequestItemRepository;
import com.ksig.tu_vcs.repos.ChangeRequestRepository;
import com.ksig.tu_vcs.repos.RepositoryRepository;
import com.ksig.tu_vcs.repos.entities.ChangeRequest;
import com.ksig.tu_vcs.repos.entities.ChangeRequestItem;
import com.ksig.tu_vcs.repos.entities.enums.ChangeRequestStatus;
import com.ksig.tu_vcs.services.views.CreateCRView;
import com.ksig.tu_vcs.services.views.ItemView;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ChangeRequestService {
    private final RepositoryRepository repositoryRepository;
    private final AppUserRepository appUserRepository;
    private final ChangeRequestRepository changeRequestRepository;
    private final ChangeRequestItemRepository changeRequestItemRepository;

    public ChangeRequestService(RepositoryRepository repositoryRepository, AppUserRepository appUserRepository, ChangeRequestRepository changeRequestRepository, ChangeRequestItemRepository changeRequestItemRepository) {
        this.repositoryRepository = repositoryRepository;
        this.appUserRepository = appUserRepository;
        this.changeRequestRepository = changeRequestRepository;
        this.changeRequestItemRepository = changeRequestItemRepository;
    }

    public ChangeRequest createChangeRequest(UUID repositoryId, UUID userId, CreateCRView view){
        ChangeRequest changeRequest = new ChangeRequest();
        changeRequest.setRepository(repositoryRepository.getReferenceById(repositoryId));
        changeRequest.setAuthor(appUserRepository.getReferenceById(userId));
        changeRequest.setBaseRevisionNumber(view.getBaseRevisionNUmber());
        changeRequest.setStatus(ChangeRequestStatus.PENDING);
        changeRequest.setTitle(view.getTittle());
        changeRequest.setDescription(view.getDescription());

        return changeRequestRepository.save(changeRequest);
    }

    @Transactional
    public ChangeRequestItem addItemToChangeRequest(UUID changeRequestId, ItemView view){
        ChangeRequest changeRequest = changeRequestRepository.findById(changeRequestId)
                .orElseThrow(()->new RuntimeException("Change request not found"));

        if (changeRequest.getStatus()!=ChangeRequestStatus.PENDING){
            throw new RuntimeException("Cannot add item to a non-pending change request");
        }

        ChangeRequestItem changeRequestItem = new ChangeRequestItem();
        changeRequestItem.setChangeRequest(changeRequest);
        changeRequestItem.setPath(view.getPath());
        changeRequestItem.setItemType(view.getItemType());
        changeRequestItem.setAction(view.getAction());
        changeRequestItem.setChecksum(view.getChecksum());
        changeRequestItem.setStorageKey(view.getFileRef());

        return changeRequestItemRepository.save(changeRequestItem);

    }
}
