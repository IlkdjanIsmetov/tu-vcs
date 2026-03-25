package com.ksig.tu_vcs.services;

import com.ksig.tu_vcs.repos.*;
import com.ksig.tu_vcs.repos.entities.AppUser;
import com.ksig.tu_vcs.repos.entities.ChangeRequest;
import com.ksig.tu_vcs.repos.entities.ChangeRequestItem;
import com.ksig.tu_vcs.repos.entities.Revision;
import com.ksig.tu_vcs.repos.entities.enums.ChangeRequestStatus;
import com.ksig.tu_vcs.services.views.CreateCRView;
import com.ksig.tu_vcs.services.views.ItemInView;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class ChangeRequestService {
    private final RepositoryRepository repositoryRepository;
    private final AppUserRepository appUserRepository;
    private final ChangeRequestRepository changeRequestRepository;
    private final ChangeRequestItemRepository changeRequestItemRepository;
    private final RevisionRepository revisionRepository;
    private final CommitService commitService;

    public ChangeRequestService(RepositoryRepository repositoryRepository, AppUserRepository appUserRepository, ChangeRequestRepository changeRequestRepository, ChangeRequestItemRepository changeRequestItemRepository, RevisionRepository revisionRepository, CommitService commitService) {
        this.repositoryRepository = repositoryRepository;
        this.appUserRepository = appUserRepository;
        this.changeRequestRepository = changeRequestRepository;
        this.changeRequestItemRepository = changeRequestItemRepository;
        this.revisionRepository = revisionRepository;
        this.commitService = commitService;
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
    public ChangeRequestItem addItemToChangeRequest(UUID changeRequestId, ItemInView view){
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

    public void approveChangeRequest(UUID repositoryId, UUID changeRequestId, List<ItemInView> items, List<MultipartFile> files, String message, AppUser currentUser){
        ChangeRequest changeRequest = changeRequestRepository.findById(changeRequestId)
                .orElseThrow(()->new RuntimeException("Change request not found"));
        Revision latestRevision = revisionRepository.findLatestRevision(repositoryId)
                .orElseThrow();
        if (!latestRevision.getRevisionNumber().equals(changeRequest.getBaseRevisionNumber())){
            changeRequest.setStatus(ChangeRequestStatus.CONFLICTED);
            throw new RuntimeException("Conflicted");
        }

        commitService.applyChange(repositoryId,items,files,message,currentUser);

    }

    public void rejectChangeRequest(UUID changeRequestId){
        ChangeRequest changeRequest = changeRequestRepository.findById(changeRequestId)
                .orElseThrow(()->new RuntimeException("Change request not found"));

        changeRequest.setStatus(ChangeRequestStatus.REJECTED);

        changeRequestRepository.save(changeRequest);
    }
}
