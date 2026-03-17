package com.ksig.tu_vcs.services;

import com.ksig.tu_vcs.repos.AppUserRepository;
import com.ksig.tu_vcs.repos.ChangeRequestRepository;
import com.ksig.tu_vcs.repos.RepositoryRepository;
import com.ksig.tu_vcs.repos.entities.ChangeRequest;
import com.ksig.tu_vcs.repos.entities.enums.ChangeRequestStatus;
import com.ksig.tu_vcs.services.views.CreateCRView;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ChangeRequestService {
    private final RepositoryRepository repositoryRepository;
    private final AppUserRepository appUserRepository;
    private final ChangeRequestRepository changeRequestRepository;

    public ChangeRequestService(RepositoryRepository repositoryRepository, AppUserRepository appUserRepository, ChangeRequestRepository changeRequestRepository) {
        this.repositoryRepository = repositoryRepository;
        this.appUserRepository = appUserRepository;
        this.changeRequestRepository = changeRequestRepository;
    }

    public ChangeRequest createChangeRequest(UUID repositoryId, UUID userId, CreateCRView view){
        ChangeRequest changeRequest = new ChangeRequest();
        changeRequest.setRepository(repositoryRepository.getReferenceById(userId));
        changeRequest.setAuthor(appUserRepository.getReferenceById(userId));
        changeRequest.setBaseRevisionNumber(view.getBaseRevisionNUmber());
        changeRequest.setStatus(ChangeRequestStatus.PENDING);
        changeRequest.setDescription(view.getDescription());

        return changeRequestRepository.save(changeRequest);
    }
}
