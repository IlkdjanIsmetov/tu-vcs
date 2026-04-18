package com.ksig.tu_vcs.services;

import com.ksig.tu_vcs.repos.entities.enums.Role;
import com.ksig.tu_vcs.services.exceptions.ResourceNotFoundException;
import com.ksig.tu_vcs.repos.AppUserRepository;
import com.ksig.tu_vcs.repos.ItemRevisionRepository;
import com.ksig.tu_vcs.repos.RepositoryMemberRepository;
import com.ksig.tu_vcs.repos.RepositoryRepository;
import com.ksig.tu_vcs.repos.entities.AppUser;
import com.ksig.tu_vcs.repos.entities.Repository;
import com.ksig.tu_vcs.repos.entities.RepositoryMember;
import com.ksig.tu_vcs.services.exceptions.ResourceAlreadyExistsException;
import com.ksig.tu_vcs.services.views.RepositoryInView;
import com.ksig.tu_vcs.services.views.RepositoryOutView;
import com.ksig.tu_vcs.utils.UserContextUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.nio.file.Path;
import java.lang.reflect.Field;

@ExtendWith(MockitoExtension.class)
class RepositoryServiceTest {

    @Mock
    private RepositoryRepository repositoryRepository;

    @Mock
    private RepositoryMemberRepository repositoryMemberRepository;

    @Mock
    private UserContextUtil userContextUtil;

    @Mock
    private ItemRevisionRepository itemRevisionRepository;

    @Mock
    private CommitService commitService;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private ConstructRepoService constructRepoService;

    @InjectMocks
    private RepositoryService repositoryService;

    @Test
    void shouldCreateRepositorySuccessfully() throws Exception {

        RepositoryInView view = new RepositoryInView();

        setField(view, "repositoryName", "TestRepo");
        setField(view, "description", "My repo");

        AppUser user = new AppUser();
        user.setUsername("admin");

        Repository savedRepo = new Repository();
        savedRepo.setName("TestRepo");
        savedRepo.setDescription("My repo");
        savedRepo.setOwner(user);
        savedRepo.setRequiresApprovalByDefault(true);

        when(repositoryRepository.findByName("TestRepo"))
                .thenReturn(Optional.empty());

        when(userContextUtil.getCurrentUser())
                .thenReturn(user);

        when(repositoryRepository.save(any(Repository.class)))
                .thenReturn(savedRepo);

        RepositoryOutView result =
                repositoryService.createRepository(view, "LOG1");

        assertNotNull(result);
        assertEquals("TestRepo", result.getName());
        assertEquals("My repo", result.getDescription());

        verify(repositoryRepository).save(any(Repository.class));
        verify(repositoryMemberRepository).save(any(RepositoryMember.class));
    }

    @Test
    void shouldThrowWhenRepositoryAlreadyExists() throws Exception {

        RepositoryInView view = new RepositoryInView();

        setField(view, "repositoryName", "TestRepo");

        when(repositoryRepository.findByName("TestRepo"))
                .thenReturn(Optional.of(new Repository()));

        assertThrows(ResourceAlreadyExistsException.class,
                () -> repositoryService.createRepository(view, "LOG1"));

        verify(repositoryRepository, never()).save(any());
        verify(repositoryMemberRepository, never()).save(any());
    }

    @Test
    void shouldDeleteRepositoryWhenUserIsMaster() {

        UUID repositoryId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AppUser user = new AppUser();
        user.setId(userId);

        RepositoryMember member = new RepositoryMember();
        member.setRole(com.ksig.tu_vcs.repos.entities.enums.Role.MASTER);

        when(userContextUtil.getCurrentUser())
                .thenReturn(user);

        when(repositoryMemberRepository
                .findByRepositoryIdAndUserId(repositoryId, userId))
                .thenReturn(Optional.of(member));

        repositoryService.deleteRepository(repositoryId, "LOG1");

        verify(repositoryMemberRepository).delete(member);
    }

    @Test
    void shouldThrowWhenUserDeletesAndIsNotMember() {

        UUID repositoryId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AppUser user = new AppUser();
        user.setId(userId);

        when(userContextUtil.getCurrentUser())
                .thenReturn(user);

        when(repositoryMemberRepository
                .findByRepositoryIdAndUserId(repositoryId, userId))
                .thenReturn(Optional.empty());

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> repositoryService.deleteRepository(repositoryId, "LOG1"));

        verify(repositoryMemberRepository, never()).delete(any());
    }

    @Test
    void shouldThrowWhenUserDeletesAndIsNotMaster() {

        UUID repositoryId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AppUser user = new AppUser();
        user.setId(userId);

        RepositoryMember member = new RepositoryMember();
        member.setRole(com.ksig.tu_vcs.repos.entities.enums.Role.CONTRIBUTOR);

        when(userContextUtil.getCurrentUser())
                .thenReturn(user);

        when(repositoryMemberRepository
                .findByRepositoryIdAndUserId(repositoryId, userId))
                .thenReturn(Optional.of(member));

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> repositoryService.deleteRepository(repositoryId, "LOG1"));

        verify(repositoryMemberRepository, never()).delete(any());
    }

    @Test
    void shouldThrowWhenUserHasNoAccess() {

        UUID repoId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AppUser user = new AppUser();
        user.setId(userId);

        when(userContextUtil.getCurrentUser())
                .thenReturn(user);

        when(repositoryMemberRepository
                .findByRepositoryIdAndUserId(repoId, userId))
                .thenReturn(Optional.empty());

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> repositoryService.fetchRevision(repoId, null));
    }

    @Test
    void shouldFetchLatestRevisionWhenRevisionIsNull() {

        UUID repoId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AppUser user = new AppUser();
        user.setId(userId);

        RepositoryMember member = new RepositoryMember();
        member.setRole(Role.CONTRIBUTOR);

        when(userContextUtil.getCurrentUser())
                .thenReturn(user);

        when(repositoryMemberRepository
                .findByRepositoryIdAndUserId(repoId, userId))
                .thenReturn(Optional.of(member));

        when(itemRevisionRepository.findLatestItemsForRepo(repoId))
                .thenReturn(List.of());

        var result = repositoryService.fetchRevision(repoId, null);

        assertNotNull(result);

        verify(itemRevisionRepository).findLatestItemsForRepo(repoId);
        verify(itemRevisionRepository, never())
                .findAllFilesAtRevision(any(), any());
    }

    @Test
    void shouldFetchSpecificRevision() {

        UUID repoId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AppUser user = new AppUser();
        user.setId(userId);

        RepositoryMember member = new RepositoryMember();
        member.setRole(Role.VIEWER);

        Long revision = 5L;

        when(userContextUtil.getCurrentUser())
                .thenReturn(user);

        when(repositoryMemberRepository
                .findByRepositoryIdAndUserId(repoId, userId))
                .thenReturn(Optional.of(member));

        when(itemRevisionRepository
                .findAllFilesAtRevision(repoId, revision))
                .thenReturn(List.of());

        var result = repositoryService.fetchRevision(repoId, revision);

        assertNotNull(result);

        verify(itemRevisionRepository)
                .findAllFilesAtRevision(repoId, revision);

        verify(itemRevisionRepository, never())
                .findLatestItemsForRepo(any());
    }

    @Test
    void shouldThrowWhenUserCommitsAndIsNotMember() {

        UUID repoId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AppUser user = new AppUser();
        user.setId(userId);

        when(userContextUtil.getCurrentUser())
                .thenReturn(user);

        when(repositoryMemberRepository
                .findByRepositoryIdAndUserId(repoId, userId))
                .thenReturn(Optional.empty());

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> repositoryService.commitDirectly(
                        repoId, List.of(), List.of(), "msg", "LOG1"
                ));

        verify(commitService, never()).applyChange(any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldThrowWhenUserCannotCommit() {

        UUID repoId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AppUser user = new AppUser();
        user.setId(userId);

        RepositoryMember member = mock(RepositoryMember.class);

        when(member.canCommit()).thenReturn(false);

        when(userContextUtil.getCurrentUser())
                .thenReturn(user);

        when(repositoryMemberRepository
                .findByRepositoryIdAndUserId(repoId, userId))
                .thenReturn(Optional.of(member));

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> repositoryService.commitDirectly(
                        repoId, List.of(), List.of(), "msg", "LOG1"
                ));

        verify(commitService, never()).applyChange(any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldCommitSuccessfully() {

        UUID repoId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AppUser user = new AppUser();
        user.setId(userId);

        RepositoryMember member = mock(RepositoryMember.class);

        when(member.canCommit()).thenReturn(true);

        when(userContextUtil.getCurrentUser())
                .thenReturn(user);

        when(repositoryMemberRepository
                .findByRepositoryIdAndUserId(repoId, userId))
                .thenReturn(Optional.of(member));

        when(commitService.applyChange(
                any(), any(), any(), any(), any(), any()
        )).thenReturn("commit-hash");

        String result = repositoryService.commitDirectly(
                repoId, List.of(), List.of(), "msg", "LOG1"
        );

        assertEquals("commit-hash", result);

        verify(commitService).applyChange(
                any(), any(), any(), any(), any(), any()
        );
    }

    @Test
    void shouldAddMemberSuccessfullyWhenUserIsMaster() {

        UUID repoId = UUID.randomUUID();

        AppUser currentUser = new AppUser();
        currentUser.setId(UUID.randomUUID());
        currentUser.setUsername("masterUser");

        AppUser userToAdd = new AppUser();
        userToAdd.setUsername("newUser");

        RepositoryMember currentMember = new RepositoryMember();
        currentMember.setRole(Role.MASTER);

        when(userContextUtil.getCurrentUser()).thenReturn(currentUser);

        when(repositoryMemberRepository
                .findByRepositoryIdAndUserId(repoId, currentUser.getId()))
                .thenReturn(Optional.of(currentMember));

        when(appUserRepository.findByUsername("newUser"))
                .thenReturn(Optional.of(userToAdd));

        when(repositoryRepository.getReferenceById(repoId))
                .thenReturn(new Repository());

        repositoryService.addMember(repoId, "newUser", Role.CONTRIBUTOR, "LOG1");

        verify(repositoryMemberRepository).save(any(RepositoryMember.class));
    }

    @Test
    void shouldThrowWhenUserIsNotMaster() {

        UUID repoId = UUID.randomUUID();

        AppUser currentUser = new AppUser();
        currentUser.setId(UUID.randomUUID());

        RepositoryMember currentMember = new RepositoryMember();
        currentMember.setRole(Role.CONTRIBUTOR);

        when(userContextUtil.getCurrentUser()).thenReturn(currentUser);

        when(repositoryMemberRepository
                .findByRepositoryIdAndUserId(repoId, currentUser.getId()))
                .thenReturn(Optional.of(currentMember));

        assertThrows(AccessDeniedException.class,
                () -> repositoryService.addMember(repoId, "newUser", Role.VIEWER, "LOG1"));

        verify(repositoryMemberRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenUserAddsMemberAndIsNotMember() {

        UUID repoId = UUID.randomUUID();

        AppUser currentUser = new AppUser();
        currentUser.setId(UUID.randomUUID());

        when(userContextUtil.getCurrentUser()).thenReturn(currentUser);

        when(repositoryMemberRepository
                .findByRepositoryIdAndUserId(repoId, currentUser.getId()))
                .thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class,
                () -> repositoryService.addMember(repoId, "newUser", Role.VIEWER, "LOG1"));

        verify(repositoryMemberRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenUsernameDoesNotExist() {

        UUID repoId = UUID.randomUUID();

        AppUser currentUser = new AppUser();
        currentUser.setId(UUID.randomUUID());

        RepositoryMember currentMember = new RepositoryMember();
        currentMember.setRole(Role.MASTER);

        when(userContextUtil.getCurrentUser()).thenReturn(currentUser);

        when(repositoryMemberRepository
                .findByRepositoryIdAndUserId(repoId, currentUser.getId()))
                .thenReturn(Optional.of(currentMember));

        when(appUserRepository.findByUsername("missingUser"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> repositoryService.addMember(repoId, "missingUser", Role.VIEWER, "LOG1"));

        verify(repositoryMemberRepository, never()).save(any());
    }

    @Test
    void shouldKickMemberSuccessfullyWhenUserIsMaster() {

        UUID repoId = UUID.randomUUID();

        AppUser currentUser = new AppUser();
        currentUser.setId(UUID.randomUUID());
        currentUser.setUsername("masterUser");

        AppUser userToKick = new AppUser();
        userToKick.setId(UUID.randomUUID());
        userToKick.setUsername("targetUser");

        RepositoryMember currentMember = new RepositoryMember();
        currentMember.setRole(Role.MASTER);

        RepositoryMember memberToKick = new RepositoryMember();

        when(userContextUtil.getCurrentUser()).thenReturn(currentUser);

        when(repositoryMemberRepository
                .findByRepositoryIdAndUserId(repoId, currentUser.getId()))
                .thenReturn(Optional.of(currentMember));

        when(appUserRepository.findByUsername("targetUser"))
                .thenReturn(Optional.of(userToKick));

        when(repositoryMemberRepository
                .findByRepositoryIdAndUserId(repoId, userToKick.getId()))
                .thenReturn(Optional.of(memberToKick));

        repositoryService.kickMember(repoId, "targetUser", "LOG1");

        verify(repositoryMemberRepository).delete(memberToKick);
    }

    @Test
    void shouldThrowWhenKickUserIsNotMaster() {

        UUID repoId = UUID.randomUUID();

        AppUser currentUser = new AppUser();
        currentUser.setId(UUID.randomUUID());

        RepositoryMember currentMember = new RepositoryMember();
        currentMember.setRole(Role.CONTRIBUTOR);

        when(userContextUtil.getCurrentUser()).thenReturn(currentUser);

        when(repositoryMemberRepository
                .findByRepositoryIdAndUserId(repoId, currentUser.getId()))
                .thenReturn(Optional.of(currentMember));

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> repositoryService.kickMember(repoId, "targetUser", "LOG1"));

        verify(repositoryMemberRepository, never()).delete(any());
    }

    @Test
    void shouldThrowWhenKickUserIsNotMember() {

        UUID repoId = UUID.randomUUID();

        AppUser currentUser = new AppUser();
        currentUser.setId(UUID.randomUUID());

        when(userContextUtil.getCurrentUser()).thenReturn(currentUser);

        when(repositoryMemberRepository
                .findByRepositoryIdAndUserId(repoId, currentUser.getId()))
                .thenReturn(Optional.empty());

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> repositoryService.kickMember(repoId, "targetUser", "LOG1"));

        verify(repositoryMemberRepository, never()).delete(any());
    }

    @Test
    void shouldThrowWhenKickUsernameDoesNotExist() {

        UUID repoId = UUID.randomUUID();

        AppUser currentUser = new AppUser();
        currentUser.setId(UUID.randomUUID());

        RepositoryMember currentMember = new RepositoryMember();
        currentMember.setRole(Role.MASTER);

        when(userContextUtil.getCurrentUser()).thenReturn(currentUser);

        when(repositoryMemberRepository
                .findByRepositoryIdAndUserId(repoId, currentUser.getId()))
                .thenReturn(Optional.of(currentMember));

        when(appUserRepository.findByUsername("missingUser"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> repositoryService.kickMember(repoId, "missingUser", "LOG1"));

        verify(repositoryMemberRepository, never()).delete(any());
    }

    @Test
    void shouldThrowWhenMemberToKickNotFound() {

        UUID repoId = UUID.randomUUID();

        AppUser currentUser = new AppUser();
        currentUser.setId(UUID.randomUUID());

        AppUser userToKick = new AppUser();
        userToKick.setId(UUID.randomUUID());

        RepositoryMember currentMember = new RepositoryMember();
        currentMember.setRole(Role.MASTER);

        when(userContextUtil.getCurrentUser()).thenReturn(currentUser);

        when(repositoryMemberRepository
                .findByRepositoryIdAndUserId(repoId, currentUser.getId()))
                .thenReturn(Optional.of(currentMember));

        when(appUserRepository.findByUsername("targetUser"))
                .thenReturn(Optional.of(userToKick));

        when(repositoryMemberRepository
                .findByRepositoryIdAndUserId(repoId, userToKick.getId()))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> repositoryService.kickMember(repoId, "targetUser", "LOG1"));
    }

    @Test
    void shouldReturnZippedRepoWhenUserHasAccess() {

        UUID repoId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AppUser user = new AppUser();
        user.setId(userId);

        RepositoryMember member = new RepositoryMember();

        Path expectedPath = mock(Path.class);

        when(userContextUtil.getCurrentUser()).thenReturn(user);

        when(repositoryMemberRepository
                .findByRepositoryIdAndUserId(repoId, userId))
                .thenReturn(Optional.of(member));

        when(constructRepoService.constructZipFolder(repoId, "LOG1"))
                .thenReturn(expectedPath);

        Path result = repositoryService.getZippedRepo(repoId, "LOG1");

        assertEquals(expectedPath, result);

        verify(constructRepoService).constructZipFolder(repoId, "LOG1");
    }

    @Test
    void shouldThrowWhenUserHasNoAccessToZipRepo() {

        UUID repoId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AppUser user = new AppUser();
        user.setId(userId);

        when(userContextUtil.getCurrentUser()).thenReturn(user);

        when(repositoryMemberRepository
                .findByRepositoryIdAndUserId(repoId, userId))
                .thenReturn(Optional.empty());

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> repositoryService.getZippedRepo(repoId, "LOG1"));

        verify(constructRepoService, never())
                .constructZipFolder(any(), any());
    }

    @Test
    void shouldReturnAllRepositories() {

        Repository repo1 = new Repository();
        repo1.setId(UUID.randomUUID());
        repo1.setName("Repo1");

        Repository repo2 = new Repository();
        repo2.setId(UUID.randomUUID());
        repo2.setName("Repo2");

        when(repositoryRepository.findAll())
                .thenReturn(List.of(repo1, repo2));

        List<RepositoryOutView> result = repositoryService.findAllRepositories();

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(repositoryRepository).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoRepositories() {

        when(repositoryRepository.findAll())
                .thenReturn(List.of());

        List<RepositoryOutView> result = repositoryService.findAllRepositories();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(repositoryRepository).findAll();
    }

    @Test
    void shouldReturnUserRepositories() {

        UUID userId = UUID.randomUUID();

        Repository repo1 = new Repository();
        repo1.setId(UUID.randomUUID());
        repo1.setName("Repo1");

        Repository repo2 = new Repository();
        repo2.setId(UUID.randomUUID());
        repo2.setName("Repo2");

        when(repositoryRepository.findByOwnerId(userId))
                .thenReturn(List.of(repo1, repo2));

        List<RepositoryOutView> result =
                repositoryService.findUserRepositories(userId);

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(repositoryRepository).findByOwnerId(userId);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoRepositories() {

        UUID userId = UUID.randomUUID();

        when(repositoryRepository.findByOwnerId(userId))
                .thenReturn(List.of());

        List<RepositoryOutView> result =
                repositoryService.findUserRepositories(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(repositoryRepository).findByOwnerId(userId);
    }

    @Test
    void shouldReturnRepositoriesMatchingSearch() {

        String search = "repo";

        Repository repo1 = new Repository();
        repo1.setId(UUID.randomUUID());
        repo1.setName("MyRepo");

        Repository repo2 = new Repository();
        repo2.setId(UUID.randomUUID());
        repo2.setName("AnotherRepo");

        when(repositoryRepository
                .findByNameContainingIgnoreCase(search))
                .thenReturn(List.of(repo1, repo2));

        List<RepositoryOutView> result =
                repositoryService.searchRepositories(search);

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(repositoryRepository)
                .findByNameContainingIgnoreCase(search);
    }

    @Test
    void shouldReturnEmptyListWhenNoRepositoriesMatchSearch() {

        String search = "missing";

        when(repositoryRepository
                .findByNameContainingIgnoreCase(search))
                .thenReturn(List.of());

        List<RepositoryOutView> result =
                repositoryService.searchRepositories(search);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(repositoryRepository)
                .findByNameContainingIgnoreCase(search);
    }

    private void setField(Object target, String fieldName, Object value)
            throws Exception {

        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
