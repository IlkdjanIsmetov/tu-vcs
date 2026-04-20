package com.ksig.tu_vcs.services;

import com.ksig.tu_vcs.repos.*;
import com.ksig.tu_vcs.repos.entities.AppUser;
import com.ksig.tu_vcs.repos.entities.ChangeRequest;
import com.ksig.tu_vcs.repos.entities.ChangeRequestItem;
import com.ksig.tu_vcs.repos.entities.RepositoryMember;
import com.ksig.tu_vcs.repos.entities.Revision;
import com.ksig.tu_vcs.repos.entities.enums.Action;
import com.ksig.tu_vcs.repos.entities.enums.ChangeRequestStatus;
import com.ksig.tu_vcs.repos.entities.enums.ItemType;
import com.ksig.tu_vcs.repos.entities.enums.Role;
import com.ksig.tu_vcs.services.views.ChangeRequestOutView;
import com.ksig.tu_vcs.services.views.CreateCRView;
import com.ksig.tu_vcs.services.views.ItemInView;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChangeRequestService {
    private final RepositoryRepository repositoryRepository;
    private final AppUserRepository appUserRepository;
    private final ChangeRequestRepository changeRequestRepository;
    private final ChangeRequestItemRepository changeRequestItemRepository;
    private final RevisionRepository revisionRepository;
    private final CommitService commitService;
    private final RepositoryMemberRepository repositoryMemberRepository;

    public ChangeRequestService(RepositoryRepository repositoryRepository, AppUserRepository appUserRepository,
                                ChangeRequestRepository changeRequestRepository, ChangeRequestItemRepository changeRequestItemRepository,
                                RevisionRepository revisionRepository, CommitService commitService,
                                RepositoryMemberRepository repositoryMemberRepository) {
        this.repositoryRepository = repositoryRepository;
        this.appUserRepository = appUserRepository;
        this.changeRequestRepository = changeRequestRepository;
        this.changeRequestItemRepository = changeRequestItemRepository;
        this.revisionRepository = revisionRepository;
        this.commitService = commitService;
        this.repositoryMemberRepository = repositoryMemberRepository;
    }

    public ChangeRequest createChangeRequest(UUID repositoryId, AppUser currentUser, CreateCRView view) {
        Optional<RepositoryMember> currentMember =
                repositoryMemberRepository.findByRepositoryIdAndUserId(repositoryId, currentUser.getId());
        if (currentMember.isEmpty() || !currentMember.get().canCommit()) {
            throw new AccessDeniedException("You cannot make change to this repository.");
        }

        ChangeRequest changeRequest = new ChangeRequest();
        changeRequest.setRepository(repositoryRepository.getReferenceById(repositoryId));
        changeRequest.setAuthor(appUserRepository.getReferenceById(currentUser.getId()));
        changeRequest.setBaseRevisionNumber(view.getBaseRevisionNUmber());
        changeRequest.setStatus(ChangeRequestStatus.PENDING);
        changeRequest.setTitle(view.getTittle());
        changeRequest.setDescription(view.getDescription());

        return changeRequestRepository.save(changeRequest);
    }

    @Transactional
    public void addItemToChangeRequest(UUID repositoryId, AppUser currentUser,UUID changeRequestId, List<ItemInView> views, List<MultipartFile> files, String logId) {
        Optional<RepositoryMember> currentMember =
                repositoryMemberRepository.findByRepositoryIdAndUserId(repositoryId, currentUser.getId());
        if (currentMember.isEmpty() || !currentMember.get().canCommit()) {
            throw new AccessDeniedException("You cannot make change to this repository.");
        }

        ChangeRequest changeRequest = changeRequestRepository.findById(changeRequestId)
                .orElseThrow(() -> new RuntimeException("Change request not found"));

        if (changeRequest.getStatus() != ChangeRequestStatus.PENDING) {
            throw new RuntimeException("Cannot add item to a non-pending change request");
        }
        //правя листа към мап с ключ името на файла, като разчитам че клиента ще опише връзките в ItemView
        Map<String, MultipartFile> fileMap = files.stream()
                .collect(Collectors.toMap(MultipartFile::getOriginalFilename, file -> file));

        for (ItemInView view : views) {
            ChangeRequestItem changeRequestItem = new ChangeRequestItem();
            changeRequestItem.setChangeRequest(changeRequest);
            changeRequestItem.setPath(view.getPath());
            changeRequestItem.setItemType(view.getItemType());
            changeRequestItem.setAction(view.getAction());
            if (view.getAction() != Action.DELETE && view.getItemType() == ItemType.FILE) {
                MultipartFile file = fileMap.get(view.getFileRef());

                if (file == null) {
                    throw new RuntimeException("Missing multipart file payload for reference: " + view.getFileRef());
                }

                String storageKey = commitService.saveFileToStorage(file, logId);

                changeRequestItem.setStorageKey(storageKey);
                changeRequestItem.setFileSize(file.getSize());
                changeRequestItem.setChecksum(view.getChecksum());

            } else {
                changeRequestItem.setStorageKey(null);
                changeRequestItem.setFileSize(0L);
                changeRequestItem.setChecksum(null);
            }

            changeRequestItemRepository.save(changeRequestItem);
        }
    }

    @Transactional
    public void approveChangeRequest(UUID repositoryId, UUID changeRequestId, AppUser currentUser) {
        Optional<RepositoryMember> currentMember =
                repositoryMemberRepository.findByRepositoryIdAndUserId(repositoryId, currentUser.getId());
        if (currentMember.isEmpty() || !currentMember.get().getRole().equals(Role.MASTER)) {
            throw new AccessDeniedException("Only MASTER members can approve this change.");
        }

        ChangeRequest changeRequest = changeRequestRepository.findById(changeRequestId)
                .orElseThrow(() -> new RuntimeException("Change request not found"));
        Revision latestRevision = revisionRepository.findLatestRevision(repositoryId)
                .orElseThrow();
        if (!latestRevision.getRevisionNumber().equals(changeRequest.getBaseRevisionNumber())) {
            changeRequest.setStatus(ChangeRequestStatus.CONFLICTED);
            throw new RuntimeException("Conflicted");
        }
        Revision newRevision = commitService.createRevision(repositoryId, changeRequest.getTitle(), currentUser);
        List<ChangeRequestItem> items = changeRequestItemRepository.findByChangeRequestId(changeRequest.getId());

        for (ChangeRequestItem crItem : items) {
            commitService.applyChangeFromCr(crItem, newRevision, repositoryId);
        }
        changeRequest.setStatus(ChangeRequestStatus.APPROVED);
        changeRequestRepository.save(changeRequest);

    }

    public void rejectChangeRequest(UUID repositoryId, AppUser currentUser ,UUID changeRequestId) {
        Optional<RepositoryMember> currentMember =
                repositoryMemberRepository.findByRepositoryIdAndUserId(repositoryId, currentUser.getId());
        if (currentMember.isEmpty() || !currentMember.get().getRole().equals(Role.MASTER)) {
            throw new AccessDeniedException("Only MASTER members can reject this change.");
        }

        ChangeRequest changeRequest = changeRequestRepository.findById(changeRequestId)
                .orElseThrow(() -> new RuntimeException("Change request not found"));

        changeRequest.setStatus(ChangeRequestStatus.REJECTED);

        changeRequestRepository.save(changeRequest);
    }

    public List<ChangeRequestOutView> showPendingRequests(UUID repositoryId, String logId){
        log.info("{}:Showing pending requests from repository: {}", logId, repositoryId);
        return changeRequestRepository.findByRepositoryIdAndStatus(repositoryId,ChangeRequestStatus.PENDING).stream()
                .map(ChangeRequestOutView::fromEntity)
                .collect(Collectors.toList());
    }

    public long countPendingRequests(UUID repositoryId){
        return changeRequestRepository.countByRepositoryIdAndStatus(repositoryId,ChangeRequestStatus.PENDING);
    }

}
