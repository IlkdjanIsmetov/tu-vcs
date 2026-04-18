package com.ksig.tu_vcs.controllers;

import com.ksig.tu_vcs.repos.entities.AppUser;
import com.ksig.tu_vcs.repos.entities.enums.Role;
import com.ksig.tu_vcs.services.RepositoryService;
import com.ksig.tu_vcs.services.views.ItemInView;
import com.ksig.tu_vcs.services.views.ItemOutView;
import com.ksig.tu_vcs.services.views.RepositoryInView;
import com.ksig.tu_vcs.services.views.RepositoryOutView;
import com.ksig.tu_vcs.utils.UserContextUtil;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.Resource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepositoryControllerTest {

    @Mock
    private RepositoryService repositoryService;

    @Mock
    private UserContextUtil userContextUtil;

    @InjectMocks
    private RepositoryController repositoryController;

    @Test
    void shouldCreateRepository() {

        RepositoryInView view = new RepositoryInView();

        RepositoryOutView out = new RepositoryOutView();
        out.setId(UUID.randomUUID());

        HttpServletRequest request = mock(HttpServletRequest.class);

        when(repositoryService.createRepository(any(), any()))
                .thenReturn(out);

        ResponseEntity<RepositoryOutView> response =
                repositoryController.createRepository(view, request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getUrl());

        verify(repositoryService).createRepository(any(), any());
        verify(request).setAttribute(eq("logId"), any());
    }

    @Test
    void shouldDeleteRepository() {

        UUID repoId = UUID.randomUUID();

        HttpServletRequest request = mock(HttpServletRequest.class);

        ResponseEntity<String> response =
                repositoryController.deleteRepository(repoId, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OK", response.getBody());

        verify(repositoryService).deleteRepository(eq(repoId), any());
        verify(request).setAttribute(eq("logId"), any());
    }

    @Test
    void shouldCommitSuccessfully() {

        UUID repoId = UUID.randomUUID();

        HttpServletRequest request = mock(HttpServletRequest.class);

        ItemInView item = mock(ItemInView.class);
        MultipartFile file = mock(MultipartFile.class);

        when(repositoryService.commitDirectly(
                eq(repoId), anyList(), anyList(), eq("msg"), any()))
                .thenReturn("OK");

        ResponseEntity<String> response =
                repositoryController.commit(
                        repoId,
                        List.of(item),
                        List.of(file),
                        "msg",
                        request
                );

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OK", response.getBody());

        verify(repositoryService).commitDirectly(
                eq(repoId), anyList(), anyList(), eq("msg"), any());
        verify(request).setAttribute(eq("logId"), any());
    }

    @Test
    void shouldFetchItemsWithRevision() {

        UUID repoId = UUID.randomUUID();

        HttpServletRequest request = mock(HttpServletRequest.class);

        ItemOutView item = mock(ItemOutView.class);

        when(repositoryService.fetchRevision(repoId, 5L))
                .thenReturn(List.of(item));

        ResponseEntity<List<ItemOutView>> response =
                repositoryController.fetchItmes(repoId, 5L, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());

        verify(repositoryService).fetchRevision(repoId, 5L);
        verify(request).setAttribute(eq("logId"), any());
    }

    @Test
    void shouldFetchItemsWithoutRevision() {

        UUID repoId = UUID.randomUUID();

        HttpServletRequest request = mock(HttpServletRequest.class);

        when(repositoryService.fetchRevision(repoId, null))
                .thenReturn(List.of());

        ResponseEntity<List<ItemOutView>> response =
                repositoryController.fetchItmes(repoId, null, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());

        verify(repositoryService).fetchRevision(repoId, null);
        verify(request).setAttribute(eq("logId"), any());
    }

    @Test
    void shouldAddMember() {

        UUID repoId = UUID.randomUUID();

        HttpServletRequest request = mock(HttpServletRequest.class);

        ResponseEntity<String> response =
                repositoryController.addMember(repoId, "user", Role.CONTRIBUTOR, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OK", response.getBody());

        verify(repositoryService).addMember(eq(repoId), eq("user"), eq(Role.CONTRIBUTOR), any());
        verify(request).setAttribute(eq("logId"), any());
    }

    @Test
    void shouldKickMember() {

        UUID repoId = UUID.randomUUID();

        HttpServletRequest request = mock(HttpServletRequest.class);

        ResponseEntity<String> response =
                repositoryController.kickMember(repoId, "user", request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OK", response.getBody());

        verify(repositoryService).kickMember(eq(repoId), eq("user"), any());
        verify(request).setAttribute(eq("logId"), any());
    }

    @Test
    void shouldDownloadZippedRepo() throws Exception {

        UUID repoId = UUID.randomUUID();

        HttpServletRequest request = mock(HttpServletRequest.class);

        Path tempFile = Files.createTempFile("repo", ".zip");
        Files.writeString(tempFile, "data");

        when(repositoryService.getZippedRepo(repoId, any()))
                .thenReturn(tempFile);

        ResponseEntity<Resource> response =
                repositoryController.downloadZippedRepo(repoId, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals("application/zip",
                response.getHeaders().getContentType().toString());

        assertTrue(response.getHeaders()
                .getFirst(HttpHeaders.CONTENT_DISPOSITION)
                .contains(".zip"));

        verify(repositoryService).getZippedRepo(eq(repoId), any());
        verify(request).setAttribute(eq("logId"), any());
    }

    @Test
    void shouldReturnAllRepositories() {

        HttpServletRequest request = mock(HttpServletRequest.class);

        RepositoryOutView repo = new RepositoryOutView();

        when(repositoryService.findAllRepositories())
                .thenReturn(List.of(repo));

        ResponseEntity<List<RepositoryOutView>> response =
                repositoryController.showAllRepositories(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());

        verify(repositoryService).findAllRepositories();
        verify(request).setAttribute(eq("logId"), any());
    }

    @Test
    void shouldReturnMyRepositories() {

        HttpServletRequest request = mock(HttpServletRequest.class);

        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());

        RepositoryOutView repo = new RepositoryOutView();

        when(userContextUtil.getCurrentUser()).thenReturn(user);
        when(repositoryService.findUserRepositories(user.getId()))
                .thenReturn(List.of(repo));

        ResponseEntity<List<RepositoryOutView>> response =
                repositoryController.getMyRepositories(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());

        verify(userContextUtil).getCurrentUser();
        verify(repositoryService).findUserRepositories(user.getId());
        verify(request).setAttribute(eq("logId"), any());
    }

    @Test
    void shouldSearchRepositories() {

        HttpServletRequest request = mock(HttpServletRequest.class);

        RepositoryOutView repo = new RepositoryOutView();

        when(repositoryService.searchRepositories("test"))
                .thenReturn(List.of(repo));

        ResponseEntity<List<RepositoryOutView>> response =
                repositoryController.searchRepositories("test", request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());

        verify(repositoryService).searchRepositories("test");
        verify(request).setAttribute(eq("logId"), any());
    }
    @Test
    void shouldReturnLatestRevisionNumber() {

        UUID repoId = UUID.randomUUID();

        HttpServletRequest request = mock(HttpServletRequest.class);

        when(repositoryService.fetchRevision(repoId, null))
                .thenReturn(List.of()); // must match return type

        ResponseEntity<List<ItemOutView>> response =
                repositoryController.fetchItmes(repoId, null, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(repositoryService).fetchRevision(repoId, null);
        verify(request).setAttribute(eq("logId"), any());
    }
}