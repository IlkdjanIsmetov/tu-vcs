package com.ksig.tu_vcs.services;

import com.ksig.tu_vcs.repos.ItemRevisionRepository;
import com.ksig.tu_vcs.repos.entities.enums.SyncStatus;
import com.ksig.tu_vcs.services.exceptions.ResourceNotFoundException;
import com.ksig.tu_vcs.services.views.ItemOutView;
import com.ksig.tu_vcs.services.views.LocalItemMetadata;
import com.ksig.tu_vcs.services.views.SyncItemView;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.lang.reflect.Field;

@ExtendWith(MockitoExtension.class)
class PullServiceTest {

    @Mock
    private ItemRevisionRepository itemRevisionRepository;

    @Mock
    private RepositoryService repositoryService;

    @InjectMocks
    private PullService pullService;

    @Test
    void shouldMarkFileAsNewRemote() {

        UUID repoId = UUID.randomUUID();

        ItemOutView remote = mock(ItemOutView.class);
        when(remote.getPath()).thenReturn("file.txt");

        when(itemRevisionRepository.findLatestItemsForRepo(repoId))
                .thenReturn(List.of(remote));

        List<LocalItemMetadata> localManifest = List.of();

        List<SyncItemView> result =
                pullService.checkSyncStatus(repoId, localManifest, "LOG1");

        assertEquals(1, result.size());
        assertEquals("file.txt", result.get(0).getPath());
        assertEquals(SyncStatus.NEW_REMOTE, result.get(0).getStatus());
    }

    @Test
    void shouldProcessExistingFile() {

        UUID repoId = UUID.randomUUID();

        ItemOutView remote = mock(ItemOutView.class);
        when(remote.getPath()).thenReturn("file.txt");
        when(remote.getChecksum()).thenReturn("abc");

        LocalItemMetadata local = mock(LocalItemMetadata.class);
        when(local.getPath()).thenReturn("file.txt");
        when(local.getChecksum()).thenReturn("abc");

        when(itemRevisionRepository.findLatestItemsForRepo(repoId))
                .thenReturn(List.of(remote));

        List<SyncItemView> result =
                pullService.checkSyncStatus(repoId, List.of(local), "LOG1");

        assertEquals(1, result.size());
        assertNotNull(result.get(0).getStatus());
    }

    @Test
    void shouldMarkFileAsDeletedRemote() {

        UUID repoId = UUID.randomUUID();

        when(itemRevisionRepository.findLatestItemsForRepo(repoId))
                .thenReturn(List.of());

        LocalItemMetadata local = mock(LocalItemMetadata.class);
        when(local.getPath()).thenReturn("file.txt");

        List<SyncItemView> result =
                pullService.checkSyncStatus(repoId, List.of(local), "LOG1");

        assertEquals(1, result.size());
        assertEquals("file.txt", result.get(0).getPath());
        assertEquals(SyncStatus.DELETED_REMOTE, result.get(0).getStatus());
    }

    @Test
    void shouldReturnResourceWhenFileExists() throws Exception {

        UUID repoId = UUID.randomUUID();
        String storageKey = "test.txt";

        Path storageDir = Path.of(System.getProperty("user.home"), "tuVCS_TEST_STORAGE");
        Files.createDirectories(storageDir);

        Path file = storageDir.resolve(storageKey);
        Files.writeString(file, "data");

        when(repositoryService.fetchRevision(repoId, null))
                .thenReturn(List.of());

        Path result =
                pullService.pullFileContent(repoId, storageKey, "LOG1");

        assertNotNull(result);
        assertTrue(Files.exists(result));
        assertEquals(file.toAbsolutePath(), result.toAbsolutePath());
    }

    @Test
    void shouldThrowWhenPullFileDoesNotExist() throws Exception {

        UUID repoId = UUID.randomUUID();
        String storageKey = "missing.txt";

        Path storageDir = Path.of(System.getProperty("user.home"), "tuVCS_TEST_STORAGE");
        Files.createDirectories(storageDir);

        when(repositoryService.fetchRevision(repoId, null))
                .thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class,
                () -> pullService.pullFileContent(repoId, storageKey, "LOG1"));
    }

    @Test
    void shouldThrowWhenUserHasNoAccessToPullFile() {

        UUID repoId = UUID.randomUUID();

        doThrow(new org.springframework.security.access.AccessDeniedException("denied"))
                .when(repositoryService)
                .fetchRevision(repoId, null);

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> pullService.pullFileContent(repoId, "file.txt", "LOG1"));
    }

    @Test
    void shouldBeUpToDateWhenChecksumsMatch() {

        UUID repoId = UUID.randomUUID();

        ItemOutView remote = mock(ItemOutView.class);
        when(remote.getPath()).thenReturn("file.txt");
        when(remote.getChecksum()).thenReturn("abc");

        LocalItemMetadata local = mock(LocalItemMetadata.class);
        when(local.getPath()).thenReturn("file.txt");
        when(local.getChecksum()).thenReturn("abc");

        when(itemRevisionRepository.findLatestItemsForRepo(repoId))
                .thenReturn(List.of(remote));

        List<SyncItemView> result =
                pullService.checkSyncStatus(repoId, List.of(local), "LOG1");

        assertEquals(SyncStatus.UP_TO_DATE, result.get(0).getStatus());
    }

    @Test
    void shouldBeModifiedRemote() {

        UUID repoId = UUID.randomUUID();

        ItemOutView remote = mock(ItemOutView.class);
        when(remote.getPath()).thenReturn("file.txt");
        when(remote.getChecksum()).thenReturn("remote");

        LocalItemMetadata local = mock(LocalItemMetadata.class);
        when(local.getPath()).thenReturn("file.txt");
        when(local.getChecksum()).thenReturn("base");
        when(local.getLastPulledRevisionNumber()).thenReturn(1L);

        when(remote.getId()).thenReturn(UUID.randomUUID());

        when(itemRevisionRepository.findLatestItemsForRepo(repoId))
                .thenReturn(List.of(remote));

        when(itemRevisionRepository
                .findChecksumAtOrBeforeRevision(any(), any()))
                .thenReturn(Optional.of("base"));

        List<SyncItemView> result =
                pullService.checkSyncStatus(repoId, List.of(local), "LOG1");

        assertEquals(SyncStatus.MODIFIED_REMOTE, result.get(0).getStatus());
    }

    @Test
    void shouldDetectConflict() {

        UUID repoId = UUID.randomUUID();

        ItemOutView remote = mock(ItemOutView.class);
        when(remote.getPath()).thenReturn("file.txt");
        when(remote.getChecksum()).thenReturn("remote");

        LocalItemMetadata local = mock(LocalItemMetadata.class);
        when(local.getPath()).thenReturn("file.txt");
        when(local.getChecksum()).thenReturn("local");
        when(local.getLastPulledRevisionNumber()).thenReturn(1L);

        when(remote.getId()).thenReturn(UUID.randomUUID());

        when(itemRevisionRepository.findLatestItemsForRepo(repoId))
                .thenReturn(List.of(remote));

        when(itemRevisionRepository
                .findChecksumAtOrBeforeRevision(any(), any()))
                .thenReturn(Optional.of("base"));

        List<SyncItemView> result =
                pullService.checkSyncStatus(repoId, List.of(local), "LOG1");

        assertEquals(SyncStatus.CONFLICT, result.get(0).getStatus());
    }

    @Test
    void shouldBeUpToDateWhenBaseChecksumIsNull() {

        UUID repoId = UUID.randomUUID();

        ItemOutView remote = mock(ItemOutView.class);
        when(remote.getPath()).thenReturn("file.txt");
        when(remote.getChecksum()).thenReturn("remote");

        LocalItemMetadata local = mock(LocalItemMetadata.class);
        when(local.getPath()).thenReturn("file.txt");
        when(local.getChecksum()).thenReturn("local");
        when(local.getLastPulledRevisionNumber()).thenReturn(1L);

        when(remote.getId()).thenReturn(UUID.randomUUID());

        when(itemRevisionRepository.findLatestItemsForRepo(repoId))
                .thenReturn(List.of(remote));

        when(itemRevisionRepository
                .findChecksumAtOrBeforeRevision(any(), any()))
                .thenReturn(Optional.empty());

        List<SyncItemView> result =
                pullService.checkSyncStatus(repoId, List.of(local), "LOG1");

        assertEquals(SyncStatus.UP_TO_DATE, result.get(0).getStatus());
    }

    @Test
    void shouldReturnFileContent() throws Exception {

        String storageKey = "test.txt";

        Path root = Path.of(CommitService.ROOT_DOWNLOAD_PATH);
        Files.createDirectories(root);

        Path file = root.resolve(storageKey);
        Files.writeString(file, "hello");

        String result = pullService.loadFileContent(storageKey, "LOG1");

        assertEquals("hello", result);
    }

    @Test
    void shouldThrowWhenLoadFileDoesNotExist() throws Exception {

        Path root = Path.of(CommitService.ROOT_DOWNLOAD_PATH);
        Files.createDirectories(root);

        assertThrows(IOException.class,
                () -> pullService.loadFileContent("missing.txt", "LOG1"));
    }

    @Test
    void shouldReturnStorageKeyAtRevision() {

        UUID repoId = UUID.randomUUID();
        String filePath = "file.txt";
        Long revision = 5L;

        when(itemRevisionRepository.findStorageKeyAtOrBeforeRevision(
                eq(repoId), eq(revision), eq(filePath), any()))
                .thenReturn(List.of("key1"));

        String result = pullService.getStorageKey(repoId, filePath, revision, "LOG1");

        assertEquals("key1", result);
    }

    @Test
    void shouldThrowWhenNoStorageKeyAtRevision() {

        UUID repoId = UUID.randomUUID();
        String filePath = "file.txt";
        Long revision = 5L;

        when(itemRevisionRepository.findStorageKeyAtOrBeforeRevision(
                eq(repoId), eq(revision), eq(filePath), any()))
                .thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class,
                () -> pullService.getStorageKey(repoId, filePath, revision, "LOG1"));
    }

    @Test
    void shouldReturnLatestStorageKey() {

        UUID repoId = UUID.randomUUID();
        String filePath = "file.txt";

        when(itemRevisionRepository.findLatestStorageKey(
                eq(repoId), eq(filePath), any()))
                .thenReturn(List.of("latest-key"));

        String result = pullService.getStorageKey(repoId, filePath, null, "LOG1");

        assertEquals("latest-key", result);
    }

    @Test
    void shouldThrowWhenNoLatestStorageKey() {

        UUID repoId = UUID.randomUUID();
        String filePath = "file.txt";

        when(itemRevisionRepository.findLatestStorageKey(
                eq(repoId), eq(filePath), any()))
                .thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class,
                () -> pullService.getStorageKey(repoId, filePath, null, "LOG1"));
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}