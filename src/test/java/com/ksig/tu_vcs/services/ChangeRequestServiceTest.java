package com.ksig.tu_vcs.services;

import com.ksig.tu_vcs.repos.*;
import com.ksig.tu_vcs.repos.entities.*;
import com.ksig.tu_vcs.repos.entities.enums.Action;
import com.ksig.tu_vcs.repos.entities.enums.ChangeRequestStatus;
import com.ksig.tu_vcs.repos.entities.enums.ItemType;
import com.ksig.tu_vcs.repos.entities.enums.Role;
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

    @Mock
    private RepositoryMemberRepository repositoryMemberRepository;

    @InjectMocks
    private ChangeRequestService changeRequestService;

    @Test
    void shouldCreateChangeRequest() {

        UUID repoId = UUID.randomUUID();

        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());

        CreateCRView view = mock(CreateCRView.class);
        when(view.getBaseRevisionNUmber()).thenReturn(3L);
        when(view.getTittle()).thenReturn("title");
        when(view.getDescription()).thenReturn("desc");

        RepositoryMember member = new RepositoryMember();
        member.setRole(Role.CONTRIBUTOR);

        when(repositoryMemberRepository.findByRepositoryIdAndUserId(repoId, user.getId()))
                .thenReturn(Optional.of(member));

        when(repositoryRepository.getReferenceById(repoId)).thenReturn(new Repository());
        when(appUserRepository.getReferenceById(user.getId())).thenReturn(user);

        ChangeRequest saved = new ChangeRequest();
        saved.setTitle("title");

        when(changeRequestRepository.save(any())).thenReturn(saved);

        ChangeRequest result =
                changeRequestService.createChangeRequest(repoId, user, view);

        assertEquals("title", result.getTitle());
    }

    @Test
    void shouldAddItemToChangeRequest() {

        UUID repoId = UUID.randomUUID();
        UUID crId = UUID.randomUUID();

        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());

        RepositoryMember member = new RepositoryMember();
        member.setRole(Role.CONTRIBUTOR);

        when(repositoryMemberRepository.findByRepositoryIdAndUserId(repoId, user.getId()))
                .thenReturn(Optional.of(member));

        ChangeRequest cr = new ChangeRequest();
        cr.setStatus(ChangeRequestStatus.PENDING);

        when(changeRequestRepository.findById(crId))
                .thenReturn(Optional.of(cr));

        ItemInView view = mock(ItemInView.class);
        when(view.getPath()).thenReturn("file.txt");
        when(view.getItemType()).thenReturn(ItemType.FILE);
        when(view.getAction()).thenReturn(Action.ADD);
        when(view.getChecksum()).thenReturn("abc");

        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("file.txt");
        when(file.getSize()).thenReturn(100L);

        when(commitService.saveFileToStorage(file, "LOG1"))
                .thenReturn("key");

        changeRequestService.addItemToChangeRequest(
                repoId,
                user,
                crId,
                List.of(view),
                List.of(file),
                "LOG1"
        );

        verify(commitService).saveFileToStorage(file, "LOG1");
        verify(changeRequestItemRepository).save(any());
    }

    @Test
    void shouldThrowWhenAddingItemChangeRequestNotFound() {

        UUID repoId = UUID.randomUUID();
        UUID crId = UUID.randomUUID();

        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());

        RepositoryMember member = new RepositoryMember();
        member.setRole(Role.CONTRIBUTOR);

        when(repositoryMemberRepository.findByRepositoryIdAndUserId(repoId, user.getId()))
                .thenReturn(Optional.of(member));

        when(changeRequestRepository.findById(crId))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> changeRequestService.addItemToChangeRequest(
                        repoId,
                        user,
                        crId,
                        List.of(mock(ItemInView.class)),
                        List.of(mock(MultipartFile.class)),
                        "LOG1"
                ));
    }

    @Test
    void shouldThrowWhenChangeRequestNotPending() {

        UUID repoId = UUID.randomUUID();
        UUID crId = UUID.randomUUID();

        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());

        RepositoryMember member = new RepositoryMember();
        member.setRole(Role.CONTRIBUTOR);

        when(repositoryMemberRepository.findByRepositoryIdAndUserId(repoId, user.getId()))
                .thenReturn(Optional.of(member));

        ChangeRequest cr = new ChangeRequest();
        cr.setStatus(ChangeRequestStatus.APPROVED);

        when(changeRequestRepository.findById(crId))
                .thenReturn(Optional.of(cr));

        assertThrows(RuntimeException.class,
                () -> changeRequestService.addItemToChangeRequest(
                        repoId,
                        user,
                        crId,
                        List.of(mock(ItemInView.class)),
                        List.of(mock(MultipartFile.class)),
                        "LOG1"
                ));
    }

    @Test
    void shouldHandleDeleteAction() {

        UUID repoId = UUID.randomUUID();
        UUID crId = UUID.randomUUID();

        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());

        RepositoryMember member = new RepositoryMember();
        member.setRole(Role.CONTRIBUTOR);

        when(repositoryMemberRepository.findByRepositoryIdAndUserId(repoId, user.getId()))
                .thenReturn(Optional.of(member));

        ChangeRequest cr = new ChangeRequest();
        cr.setStatus(ChangeRequestStatus.PENDING);

        when(changeRequestRepository.findById(crId))
                .thenReturn(Optional.of(cr));

        ItemInView view = mock(ItemInView.class);
        when(view.getAction()).thenReturn(Action.DELETE);
        when(view.getPath()).thenReturn("file.txt");
        when(view.getItemType()).thenReturn(ItemType.FILE);

        changeRequestService.addItemToChangeRequest(
                repoId,
                user,
                crId,
                List.of(view),
                List.of(mock(MultipartFile.class)),
                "LOG1"
        );

        verify(commitService, never()).saveFileToStorage(any(), any());
        verify(changeRequestItemRepository).save(any());
    }

    @Test
    void shouldApproveChangeRequest() {

        UUID repoId = UUID.randomUUID();
        UUID crId = UUID.randomUUID();

        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());

        RepositoryMember member = new RepositoryMember();
        member.setRole(Role.MASTER);

        when(repositoryMemberRepository.findByRepositoryIdAndUserId(repoId, user.getId()))
                .thenReturn(Optional.of(member));

        ChangeRequest cr = new ChangeRequest();
        cr.setId(crId);
        cr.setBaseRevisionNumber(1L);
        cr.setTitle("title");

        Revision latest = new Revision();
        latest.setRevisionNumber(1L);

        Revision newRevision = new Revision();

        when(changeRequestRepository.findById(crId)).thenReturn(Optional.of(cr));
        when(revisionRepository.findLatestRevision(repoId)).thenReturn(Optional.of(latest));
        when(commitService.createRevision(repoId, "title", user)).thenReturn(newRevision);
        when(changeRequestItemRepository.findByChangeRequestId(crId)).thenReturn(List.of(new ChangeRequestItem()));

        changeRequestService.approveChangeRequest(repoId, crId, user);

        verify(changeRequestRepository).save(cr);
    }

    @Test
    void shouldThrowWhenApprovingChangeRequestNotFound() {

        UUID repoId = UUID.randomUUID();
        UUID crId = UUID.randomUUID();

        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());

        RepositoryMember member = new RepositoryMember();
        member.setRole(Role.MASTER);

        when(repositoryMemberRepository.findByRepositoryIdAndUserId(repoId, user.getId()))
                .thenReturn(Optional.of(member));

        when(changeRequestRepository.findById(crId))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> changeRequestService.approveChangeRequest(repoId, crId, user));
    }

    @Test
    void shouldMarkConflictedWhenRevisionMismatch() {

        UUID repoId = UUID.randomUUID();
        UUID crId = UUID.randomUUID();

        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());

        RepositoryMember member = new RepositoryMember();
        member.setRole(Role.MASTER);

        when(repositoryMemberRepository.findByRepositoryIdAndUserId(repoId, user.getId()))
                .thenReturn(Optional.of(member));

        ChangeRequest cr = new ChangeRequest();
        cr.setBaseRevisionNumber(1L);

        Revision latest = new Revision();
        latest.setRevisionNumber(2L);

        when(changeRequestRepository.findById(crId))
                .thenReturn(Optional.of(cr));

        when(revisionRepository.findLatestRevision(repoId))
                .thenReturn(Optional.of(latest));

        assertThrows(RuntimeException.class,
                () -> changeRequestService.approveChangeRequest(repoId, crId, user));

        assertEquals(ChangeRequestStatus.CONFLICTED, cr.getStatus());
        verify(commitService, never()).createRevision(any(), any(), any());
    }

    @Test
    void shouldRejectChangeRequest() {

        UUID repoId = UUID.randomUUID();
        UUID crId = UUID.randomUUID();

        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());

        RepositoryMember member = new RepositoryMember();
        member.setRole(Role.MASTER);

        when(repositoryMemberRepository.findByRepositoryIdAndUserId(repoId, user.getId()))
                .thenReturn(Optional.of(member));

        ChangeRequest cr = new ChangeRequest();

        when(changeRequestRepository.findById(crId))
                .thenReturn(Optional.of(cr));

        changeRequestService.rejectChangeRequest(repoId, user, crId);

        assertEquals(ChangeRequestStatus.REJECTED, cr.getStatus());
    }

    @Test
    void shouldThrowWhenRejectingNonExistingChangeRequest() {

        UUID repoId = UUID.randomUUID();
        UUID crId = UUID.randomUUID();

        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());

        RepositoryMember member = new RepositoryMember();
        member.setRole(Role.MASTER);

        when(repositoryMemberRepository.findByRepositoryIdAndUserId(repoId, user.getId()))
                .thenReturn(Optional.of(member));

        when(changeRequestRepository.findById(crId))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> changeRequestService.rejectChangeRequest(repoId, user, crId));
    }

}