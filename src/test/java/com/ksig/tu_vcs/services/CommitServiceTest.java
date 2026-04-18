package com.ksig.tu_vcs.services;

import com.ksig.tu_vcs.repos.RepositoryRepository;
import com.ksig.tu_vcs.repos.ItemRepository;
import com.ksig.tu_vcs.repos.ItemRevisionRepository;
import com.ksig.tu_vcs.repos.RevisionRepository;
import com.ksig.tu_vcs.repos.entities.*;
import com.ksig.tu_vcs.repos.entities.enums.Action;
import com.ksig.tu_vcs.repos.entities.enums.ItemType;
import com.ksig.tu_vcs.services.exceptions.CommitException;
import com.ksig.tu_vcs.services.views.ItemInView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommitServiceTest {

    @Mock
    private RepositoryRepository repositoryRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemRevisionRepository itemRevisionRepository;

    @Mock
    private RevisionRepository revisionRepository;

    @InjectMocks
    private CommitService commitService;

    @Test
    void shouldApplyChangesSuccessfully() {

        UUID repoId = UUID.randomUUID();

        AppUser user = new AppUser();

        Revision revision = new Revision();

        CommitService spyService = spy(commitService);

        doReturn(revision)
                .when(spyService)
                .createRevision(any(), any(), any());

        ItemInView item = mock(ItemInView.class);
        when(item.getItemType()).thenReturn(ItemType.FILE);
        when(item.getAction()).thenReturn(Action.ADD);
        when(item.getFileRef()).thenReturn("file.txt");

        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("file.txt");

        String result = spyService.applyChange(
                repoId,
                List.of(item),
                List.of(file),
                "msg",
                user,
                "LOG1"
        );

        assertEquals("OK", result);
    }

    @Test
    void shouldHandleDeleteFile() {

        UUID repoId = UUID.randomUUID();

        AppUser user = new AppUser();

        Revision revision = new Revision();

        CommitService spyService = spy(commitService);

        doReturn(revision)
                .when(spyService)
                .createRevision(any(), any(), any());

        ItemInView item = mock(ItemInView.class);
        when(item.getItemType()).thenReturn(ItemType.FILE);
        when(item.getAction()).thenReturn(Action.DELETE);

        String result = spyService.applyChange(
                repoId,
                List.of(item),
                List.of(),
                "msg",
                user,
                "LOG1"
        );

        assertEquals("OK", result);
    }

    @Test
    void shouldHandleAddDirectory() {

        UUID repoId = UUID.randomUUID();

        AppUser user = new AppUser();

        Revision revision = new Revision();

        CommitService spyService = spy(commitService);

        doReturn(revision)
                .when(spyService)
                .createRevision(any(), any(), any());

        ItemInView item = mock(ItemInView.class);
        when(item.getItemType()).thenReturn(ItemType.DIRECTORY);
        when(item.getAction()).thenReturn(Action.ADD);

        String result = spyService.applyChange(
                repoId,
                List.of(item),
                List.of(),
                "msg",
                user,
                "LOG1"
        );

        assertEquals("OK", result);
    }

    @Test
    void shouldAddNewItem() {

        UUID repoId = UUID.randomUUID();

        ChangeRequestItem crItem = mock(ChangeRequestItem.class);
        when(crItem.getAction()).thenReturn(Action.ADD);
        when(crItem.getPath()).thenReturn("file.txt");
        when(crItem.getItemType()).thenReturn(ItemType.FILE);

        Revision revision = new Revision();

        when(itemRepository.findByRepositoryIdAndPath(repoId, "file.txt"))
                .thenReturn(Optional.empty());

        when(repositoryRepository.getReferenceById(repoId))
                .thenReturn(new Repository());

        Item savedItem = new Item();
        when(itemRepository.save(any(Item.class))).thenReturn(savedItem);

        commitService.applyChangeFromCr(crItem, revision, repoId);

        verify(itemRepository).save(any(Item.class));
        verify(itemRevisionRepository).save(any(ItemRevision.class));
    }

    @Test
    void shouldThrowWhenAddingExistingItem() {

        UUID repoId = UUID.randomUUID();

        ChangeRequestItem crItem = mock(ChangeRequestItem.class);
        when(crItem.getAction()).thenReturn(Action.ADD);
        when(crItem.getPath()).thenReturn("file.txt");

        when(itemRepository.findByRepositoryIdAndPath(repoId, "file.txt"))
                .thenReturn(Optional.of(new Item()));

        assertThrows(RuntimeException.class,
                () -> commitService.applyChangeFromCr(crItem, new Revision(), repoId));

        verify(itemRevisionRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenItemNotFoundForNonAddAction() {

        UUID repoId = UUID.randomUUID();

        ChangeRequestItem crItem = mock(ChangeRequestItem.class);
        when(crItem.getAction()).thenReturn(Action.MODIFY);
        when(crItem.getPath()).thenReturn("file.txt");

        when(itemRepository.findByRepositoryIdAndPath(repoId, "file.txt"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> commitService.applyChangeFromCr(crItem, new Revision(), repoId));

        verify(itemRevisionRepository, never()).save(any());
    }

    @Test
    void shouldModifyExistingItem() {

        UUID repoId = UUID.randomUUID();

        ChangeRequestItem crItem = mock(ChangeRequestItem.class);
        when(crItem.getAction()).thenReturn(Action.MODIFY);
        when(crItem.getPath()).thenReturn("file.txt");

        Item existing = new Item();

        when(itemRepository.findByRepositoryIdAndPath(repoId, "file.txt"))
                .thenReturn(Optional.of(existing));

        commitService.applyChangeFromCr(crItem, new Revision(), repoId);

        verify(itemRevisionRepository).save(any(ItemRevision.class));
    }

    @Test
    void shouldSaveFileToStorage() throws Exception {

        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream())
                .thenReturn(new ByteArrayInputStream("data".getBytes()));

        Path tempDir = Files.createTempDirectory("storage");
        setField(commitService, "ROOT_DOWNLOAD_PATH", tempDir.toString());

        String result = commitService.saveFileToStorage(file, "LOG1");

        assertNotNull(result);
        assertTrue(Files.exists(tempDir.resolve(result)));
    }

    @Test
    void shouldThrowWhenSavingFileFails() throws Exception {

        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenThrow(new IOException());

        Path tempDir = Files.createTempDirectory("storage");
        setField(commitService, "ROOT_DOWNLOAD_PATH", tempDir.toString());

        assertThrows(CommitException.class,
                () -> commitService.saveFileToStorage(file, "LOG1"));
    }

    @Test
    void shouldCreateFirstRevision() {

        UUID repoId = UUID.randomUUID();

        AppUser user = new AppUser();

        when(repositoryRepository.getReferenceById(repoId))
                .thenReturn(new Repository());

        when(revisionRepository.findLatestRevision(repoId))
                .thenReturn(Optional.empty());

        Revision saved = new Revision();
        saved.setRevisionNumber(1L);

        when(revisionRepository.save(any(Revision.class)))
                .thenReturn(saved);

        Revision result =
                commitService.createRevision(repoId, "msg", user);

        assertEquals(1L, result.getRevisionNumber());
    }

    @Test
    void shouldIncrementRevisionNumber() {

        UUID repoId = UUID.randomUUID();

        AppUser user = new AppUser();

        Revision previous = new Revision();
        previous.setRevisionNumber(5L);

        when(repositoryRepository.getReferenceById(repoId))
                .thenReturn(new Repository());

        when(revisionRepository.findLatestRevision(repoId))
                .thenReturn(Optional.of(previous));

        Revision saved = new Revision();
        saved.setRevisionNumber(6L);

        when(revisionRepository.save(any(Revision.class)))
                .thenReturn(saved);

        Revision result =
                commitService.createRevision(repoId, "msg", user);

        assertEquals(6L, result.getRevisionNumber());
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}