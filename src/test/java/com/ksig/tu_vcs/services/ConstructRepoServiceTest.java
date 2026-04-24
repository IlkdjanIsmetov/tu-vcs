package com.ksig.tu_vcs.services;

import com.ksig.tu_vcs.repos.ItemRevisionRepository;
import com.ksig.tu_vcs.repos.RepositoryRepository;
import com.ksig.tu_vcs.repos.RevisionRepository;
import com.ksig.tu_vcs.repos.entities.Repository;
import com.ksig.tu_vcs.repos.entities.Revision;
import com.ksig.tu_vcs.repos.entities.enums.ItemType;
import com.ksig.tu_vcs.services.views.ItemOutView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;

import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


//TO DO: find a solution to the infinity recursion (depth = 500 > crash)


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

    private Path tempRoot;
    private Path storageRoot;

    @AfterEach
    void cleanup() throws Exception {
        if (tempRoot != null && Files.exists(tempRoot)) {
            Files.walk(tempRoot)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(p -> {
                        try { Files.delete(p); } catch (Exception ignored) {}
                    });
        }

        if (storageRoot != null && Files.exists(storageRoot)) {
            Files.walk(storageRoot)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(p -> {
                        try { Files.delete(p); } catch (Exception ignored) {}
                    });
        }

        RequestContextHolder.resetRequestAttributes();
    }

    private void setupRequestContext() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8080);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

//    @Test
//    void shouldConstructZipFolder() throws Exception {
//
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        request.setScheme("http");
//        request.setServerName("localhost");
//        request.setServerPort(8080);
//        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
//
//        UUID repoId = UUID.randomUUID();
//
//        Repository repo = new Repository();
//        repo.setId(repoId);
//        repo.setName("repo");
//
//        Revision revision = new Revision();
//        revision.setRevisionNumber(1L);
//
//        ItemOutView file = mock(ItemOutView.class);
//        when(file.getItemType()).thenReturn(ItemType.FILE);
//        when(file.getPath()).thenReturn("file.txt");
//        when(file.getStorageKey()).thenReturn("file.txt");
//        when(file.getRevisionNumber()).thenReturn(1L);
//        when(file.getId()).thenReturn(UUID.randomUUID());
//        when(file.getChecksum()).thenReturn("abc");
//
//        Path storageRoot = Path.of(CommitService.ROOT_DOWNLOAD_PATH);
//        Files.createDirectories(storageRoot);
//        Path storedFile = storageRoot.resolve("file.txt");
//        Files.writeString(storedFile, "data");
//
//        when(itemRevisionRepository.findLatestItemsForRepo(repoId))
//                .thenReturn(List.of(file));
//
//        when(repositoryRepository.findById(repoId))
//                .thenReturn(Optional.of(repo));
//
//        when(revisionRepository.findLatestRevision(repoId))
//                .thenReturn(Optional.of(revision));
//
//        ObjectMapper safeMapper = new ObjectMapper();
//        safeMapper.isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS);
//
//        ReflectionTestUtils.setField(
//                constructRepoService,
//                "objectMapper",
//                safeMapper
//        );
//
//        Path result = constructRepoService.constructZipFolder(repoId, "LOG1");
//
//        assertNotNull(result);
//        assertTrue(Files.exists(result));
//        assertTrue(result.toString().endsWith(".zip"));
//
//        RequestContextHolder.resetRequestAttributes();
//    }
//
//    @Test
//    void shouldThrowWhenIOExceptionOccurs() throws Exception {
//
//        setupRequestContext();
//
//        UUID repoId = UUID.randomUUID();
//
//        tempRoot = Files.createTempDirectory("zip-root");
//        storageRoot = Files.createTempDirectory("storage-root");
//
//        ReflectionTestUtils.setField(
//                constructRepoService,
//                "objectMapper",
//                new ObjectMapper()
//        );
//
//        Repository repo = new Repository();
//        repo.setId(repoId);
//        repo.setName("repo");
//
//        Revision revision = new Revision();
//        revision.setRevisionNumber(1L);
//
//        ItemOutView file = mock(ItemOutView.class);
//        when(file.getItemType()).thenReturn(ItemType.FILE);
//        when(file.getPath()).thenReturn("file.txt");
//        when(file.getStorageKey()).thenReturn("missing.txt");
//        when(file.getRevisionNumber()).thenReturn(1L);
//        when(file.getId()).thenReturn(UUID.randomUUID());
//        when(file.getChecksum()).thenReturn("abc");
//
//        when(itemRevisionRepository.findLatestItemsForRepo(repoId))
//                .thenReturn(List.of(file));
//
//        when(repositoryRepository.findById(repoId))
//                .thenReturn(Optional.of(repo));
//
//        when(revisionRepository.findLatestRevision(repoId))
//                .thenReturn(Optional.of(revision));
//
//        assertThrows(RuntimeException.class,
//                () -> constructRepoService.constructZipFolder(repoId, "LOG1"));
//    }
}