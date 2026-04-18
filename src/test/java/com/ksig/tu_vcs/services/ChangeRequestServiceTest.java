package com.ksig.tu_vcs.services;

import com.ksig.tu_vcs.repos.RepositoryRepository;
import com.ksig.tu_vcs.repos.AppUserRepository;
import com.ksig.tu_vcs.repos.ChangeRequestRepository;
import com.ksig.tu_vcs.repos.ChangeRequestItemRepository;
import com.ksig.tu_vcs.repos.RevisionRepository;
import com.ksig.tu_vcs.repos.entities.*;
import com.ksig.tu_vcs.repos.entities.enums.Action;
import com.ksig.tu_vcs.repos.entities.enums.ChangeRequestStatus;
import com.ksig.tu_vcs.repos.entities.enums.ItemType;
import com.ksig.tu_vcs.services.views.CreateCRView;
import com.ksig.tu_vcs.services.views.ItemInView;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangeRequestServiceTest {

    @Mock
    private RepositoryRepository repositoryRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private ChangeRequestRepository changeRequestRepository;

    @Mock
    private ChangeRequestItemRepository changeRequestItemRepository;

    @Mock
    private RevisionRepository revisionRepository;

    @Mock
    private CommitService commitService;

    @InjectMocks
    private ChangeRequestService changeRequestService;

    @Test
    void shouldCreateChangeRequest() {

        UUID repoId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateCRView view = mock(CreateCRView.class);
        when(view.getBaseRevisionNUmber()).thenReturn(3L);
        when(view.getTittle()).thenReturn("title");
        when(view.getDescription()).thenReturn("desc");

        Repository repo = new Repository();
        AppUser user = new AppUser();

        when(repositoryRepository.getReferenceById(repoId)).thenReturn(repo);
        when(appUserRepository.getReferenceById(userId)).thenReturn(user);

        ChangeRequest saved = new ChangeRequest();
        saved.setTitle("title");

        when(changeRequestRepository.save(any(ChangeRequest.class)))
                .thenReturn(saved);

        ChangeRequest result =
                changeRequestService.createChangeRequest(repoId, userId, view);

        assertNotNull(result);
        assertEquals("title", result.getTitle());

        verify(changeRequestRepository).save(any(ChangeRequest.class));
    }

    @Test
    void shouldAddItemToChangeRequest() {

        UUID crId = UUID.randomUUID();

        ChangeRequest cr = new ChangeRequest();
        cr.setStatus(ChangeRequestStatus.PENDING);

        ItemInView view = mock(ItemInView.class);
        when(view.getPath()).thenReturn("file.txt");
        when(view.getItemType()).thenReturn(ItemType.FILE);
        when(view.getAction()).thenReturn(Action.ADD);
        when(view.getChecksum()).thenReturn("abc");

        MultipartFile file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(100L);

        when(changeRequestRepository.findById(crId))
                .thenReturn(Optional.of(cr));

        when(commitService.saveFileToStorage(file, "LOG1"))
                .thenReturn("key");

        ChangeRequestItem saved = new ChangeRequestItem();

        when(changeRequestItemRepository.save(any(ChangeRequestItem.class)))
                .thenReturn(saved);

        ChangeRequestItem result =
                changeRequestService.addItemToChangeRequest(crId, view, file, "LOG1");

        assertNotNull(result);

        verify(commitService).saveFileToStorage(file, "LOG1");
        verify(changeRequestItemRepository).save(any(ChangeRequestItem.class));
    }

    @Test
    void shouldThrowWhenAddingItemChangeRequestNotFound() {

        UUID crId = UUID.randomUUID();

        when(changeRequestRepository.findById(crId))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> changeRequestService.addItemToChangeRequest(
                        crId, mock(ItemInView.class), mock(MultipartFile.class), "LOG1"));
    }

    @Test
    void shouldThrowWhenChangeRequestNotPending() {

        UUID crId = UUID.randomUUID();

        ChangeRequest cr = new ChangeRequest();
        cr.setStatus(ChangeRequestStatus.APPROVED);

        when(changeRequestRepository.findById(crId))
                .thenReturn(Optional.of(cr));

        assertThrows(RuntimeException.class,
                () -> changeRequestService.addItemToChangeRequest(
                        crId, mock(ItemInView.class), mock(MultipartFile.class), "LOG1"));
    }

    @Test
    void shouldHandleDeleteAction() {

        UUID crId = UUID.randomUUID();

        ChangeRequest cr = new ChangeRequest();
        cr.setStatus(ChangeRequestStatus.PENDING);

        ItemInView view = mock(ItemInView.class);
        when(view.getAction()).thenReturn(Action.DELETE);
        when(view.getPath()).thenReturn("file.txt");
        when(view.getItemType()).thenReturn(ItemType.FILE);

        when(changeRequestRepository.findById(crId))
                .thenReturn(Optional.of(cr));

        ChangeRequestItem saved = new ChangeRequestItem();

        when(changeRequestItemRepository.save(any(ChangeRequestItem.class)))
                .thenReturn(saved);

        ChangeRequestItem result =
                changeRequestService.addItemToChangeRequest(
                        crId, view, mock(MultipartFile.class), "LOG1");

        assertNotNull(result);

        verify(commitService, never()).saveFileToStorage(any(), any());
    }

    @Test
    void shouldApproveChangeRequest() {

        UUID repoId = UUID.randomUUID();
        UUID crId = UUID.randomUUID();

        AppUser user = new AppUser();

        ChangeRequest cr = new ChangeRequest();
        cr.setId(crId);
        cr.setBaseRevisionNumber(1L);
        cr.setTitle("title");

        Revision latest = new Revision();
        latest.setRevisionNumber(1L);

        Revision newRevision = new Revision();

        ChangeRequestItem item = new ChangeRequestItem();

        when(changeRequestRepository.findById(crId))
                .thenReturn(Optional.of(cr));

        when(revisionRepository.findLatestRevision(repoId))
                .thenReturn(Optional.of(latest));

        when(commitService.createRevision(repoId, "title", user))
                .thenReturn(newRevision);

        when(changeRequestItemRepository.findByChangeRequestId(crId))
                .thenReturn(List.of(item));

        changeRequestService.approveChangeRequest(repoId, crId, user);

        verify(commitService).applyChangeFromCr(item, newRevision, repoId);
        verify(changeRequestRepository).save(cr);
        assertEquals(ChangeRequestStatus.APPROVED, cr.getStatus());
    }

    @Test
    void shouldThrowWhenApprovingChangeRequestNotFound() {

        UUID repoId = UUID.randomUUID();
        UUID crId = UUID.randomUUID();

        when(changeRequestRepository.findById(crId))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> changeRequestService.approveChangeRequest(repoId, crId, new AppUser()));
    }

    @Test
    void shouldMarkConflictedWhenRevisionMismatch() {

        UUID repoId = UUID.randomUUID();
        UUID crId = UUID.randomUUID();

        ChangeRequest cr = new ChangeRequest();
        cr.setBaseRevisionNumber(1L);

        Revision latest = new Revision();
        latest.setRevisionNumber(2L);

        when(changeRequestRepository.findById(crId))
                .thenReturn(Optional.of(cr));

        when(revisionRepository.findLatestRevision(repoId))
                .thenReturn(Optional.of(latest));

        assertThrows(RuntimeException.class,
                () -> changeRequestService.approveChangeRequest(repoId, crId, new AppUser()));

        assertEquals(ChangeRequestStatus.CONFLICTED, cr.getStatus());
        verify(commitService, never()).createRevision(any(), any(), any());
    }

    @Test
    void shouldRejectChangeRequest() {

        UUID crId = UUID.randomUUID();

        ChangeRequest cr = new ChangeRequest();

        when(changeRequestRepository.findById(crId))
                .thenReturn(Optional.of(cr));

        changeRequestService.rejectChangeRequest(crId);

        assertEquals(ChangeRequestStatus.REJECTED, cr.getStatus());
        verify(changeRequestRepository).save(cr);
    }

    @Test
    void shouldThrowWhenRejectingNonExistingChangeRequest() {

        UUID crId = UUID.randomUUID();

        when(changeRequestRepository.findById(crId))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> changeRequestService.rejectChangeRequest(crId));
    }

}