package com.ksig.tu_vcs.services;

import com.ksig.tu_vcs.repos.RepositoryRepository;
import com.ksig.tu_vcs.repos.ItemRevisionRepository;
import com.ksig.tu_vcs.repos.RevisionRepository;
import com.ksig.tu_vcs.repos.entities.Repository;
import com.ksig.tu_vcs.repos.entities.Revision;
import com.ksig.tu_vcs.repos.entities.enums.ItemType;
import com.ksig.tu_vcs.services.views.ItemOutView;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;

@ExtendWith(MockitoExtension.class)
class ConstructRepoServiceTest {

    @Mock
    private RepositoryRepository repositoryRepository;

    @Mock
    private ItemRevisionRepository itemRevisionRepository;

    @Mock
    private RevisionRepository revisionRepository;

    @InjectMocks
    private ConstructRepoService constructRepoService;

    @Test
    void shouldConstructZipFolder() throws Exception {

        UUID repoId = UUID.randomUUID();

        Repository repo = new Repository();
        repo.setId(repoId);
        repo.setName("repo");

        Revision revision = new Revision();
        revision.setRevisionNumber(1L);

        ItemOutView file = mock(ItemOutView.class);
        when(file.getItemType()).thenReturn(ItemType.FILE);
        when(file.getPath()).thenReturn("file.txt");
        when(file.getStorageKey()).thenReturn("file.txt");

        Path tempStorage = Files.createTempDirectory("storage");
        Path storedFile = tempStorage.resolve("file.txt");
        Files.writeString(storedFile, "data");

        Path tempZip = Files.createTempDirectory("zip");

        setField(constructRepoService, "TEMP_ZIP_DIR", tempZip.toString());
        setStaticField(CommitService.class, "ROOT_DOWNLOAD_PATH", tempStorage.toString());

        when(itemRevisionRepository.findLatestItemsForRepo(repoId))
                .thenReturn(List.of(file));

        when(repositoryRepository.findById(repoId))
                .thenReturn(Optional.of(repo));

        when(revisionRepository.findLatestRevision(repoId))
                .thenReturn(Optional.of(revision));

        Path result = constructRepoService.constructZipFolder(repoId, "LOG1");

        assertNotNull(result);
    }
    @Test
    void shouldThrowWhenIOExceptionOccurs() throws Exception {

        UUID repoId = UUID.randomUUID();

        Repository repo = new Repository();
        repo.setId(repoId);
        repo.setName("repo");

        Revision revision = new Revision();
        revision.setRevisionNumber(1L);

        ItemOutView file = mock(ItemOutView.class);
        when(file.getItemType()).thenReturn(ItemType.FILE);
        when(file.getPath()).thenReturn("file.txt");
        when(file.getStorageKey()).thenReturn("missing.txt");

        Path tempZip = Files.createTempDirectory("zip");

        setField(constructRepoService, "TEMP_ZIP_DIR", tempZip.toString());
        setStaticField(CommitService.class, "ROOT_DOWNLOAD_PATH", tempZip.toString());

        when(itemRevisionRepository.findLatestItemsForRepo(repoId))
                .thenReturn(List.of(file));

        when(repositoryRepository.findById(repoId))
                .thenReturn(Optional.of(repo));

        when(revisionRepository.findLatestRevision(repoId))
                .thenReturn(Optional.of(revision));

        assertThrows(RuntimeException.class,
                () -> constructRepoService.constructZipFolder(repoId, "LOG1"));
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private void setStaticField(Class<?> clazz, String fieldName, Object value) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }

}