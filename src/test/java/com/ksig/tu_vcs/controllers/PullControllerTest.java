package com.ksig.tu_vcs.controllers;

import com.ksig.tu_vcs.services.PullService;
import com.ksig.tu_vcs.services.views.*;

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

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PullControllerTest {

    @Mock
    private PullService pullService;

    @InjectMocks
    private PullController pullController;

    @Test
    void shouldReturnSyncStatus() {

        UUID repoId = UUID.randomUUID();

        HttpServletRequest request = mock(HttpServletRequest.class);

        LocalItemMetadata local = mock(LocalItemMetadata.class);
        SyncItemView syncItem = mock(SyncItemView.class);

        when(pullService.checkSyncStatus(eq(repoId), anyList(), any()))
                .thenReturn(List.of(syncItem));

        ResponseEntity<List<SyncItemView>> response =
                pullController.getSyncStatus(repoId, List.of(local), request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());

        verify(pullService).checkSyncStatus(eq(repoId), anyList(), any());
        verify(request).setAttribute(eq("logId"), any());
    }

    @Test
    void shouldDownloadFile() throws IOException {

        UUID repoId = UUID.randomUUID();
        String storageKey = "file.txt";

        HttpServletRequest request = mock(HttpServletRequest.class);

        Resource resource = mock(Resource.class);

        when(pullService.pullFileContent(eq(repoId), eq(storageKey), any()))
                .thenReturn(resource.getFilePath());

        ResponseEntity<Resource> response =
                pullController.downloadFile(repoId, storageKey, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resource, response.getBody());

        assertTrue(response.getHeaders()
                .getFirst(HttpHeaders.CONTENT_DISPOSITION)
                .contains(storageKey));

        verify(pullService).pullFileContent(eq(repoId), eq(storageKey), any());
        verify(request).setAttribute(eq("logId"), any());
    }

    @Test
    void shouldReturnFileContentWithRevision() throws Exception {

        UUID repoId = UUID.randomUUID();
        String path = "file.txt";

        HttpServletRequest request = mock(HttpServletRequest.class);

        when(pullService.getStorageKey(repoId, path, 5L, any()))
                .thenReturn("key");

        when(pullService.loadFileContent("key", any()))
                .thenReturn("content");

        ResponseEntity<String> response =
                pullController.getFileContent(repoId, path, 5L, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("content", response.getBody());

        verify(pullService).getStorageKey(repoId, path, 5L, any());
        verify(pullService).loadFileContent("key", any());
        verify(request).setAttribute(eq("logId"), any());
    }

    @Test
    void shouldReturnFileContentWithoutRevision() throws Exception {

        UUID repoId = UUID.randomUUID();
        String path = "file.txt";

        HttpServletRequest request = mock(HttpServletRequest.class);

        when(pullService.getStorageKey(repoId, path, null, any()))
                .thenReturn("key");

        when(pullService.loadFileContent("key", any()))
                .thenReturn("content");

        ResponseEntity<String> response =
                pullController.getFileContent(repoId, path, null, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("content", response.getBody());

        verify(pullService).getStorageKey(repoId, path, null, any());
        verify(pullService).loadFileContent("key", any());
        verify(request).setAttribute(eq("logId"), any());
    }

}