package com.ksig.tu_vcs.controllers;

import com.ksig.tu_vcs.repos.entities.AppUser;
import com.ksig.tu_vcs.repos.entities.ChangeRequest;
import com.ksig.tu_vcs.repos.entities.ChangeRequestItem;
import com.ksig.tu_vcs.services.ChangeRequestService;
import com.ksig.tu_vcs.services.views.*;
import com.ksig.tu_vcs.utils.UserContextUtil;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangeRequestControllerTest {

    @Mock
    private ChangeRequestService changeRequestService;

    @Mock
    private UserContextUtil userContextUtil;

    @InjectMocks
    private ChangeRequestController changeRequestController;

    @Test
    void shouldCreateChangeRequest() {

        UUID repoId = UUID.randomUUID();

        HttpServletRequest request = mock(HttpServletRequest.class);

        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());

        CreateCRView view = mock(CreateCRView.class);

        ChangeRequest cr = new ChangeRequest();
        UUID crId = UUID.randomUUID();
        cr.setId(crId);

        when(userContextUtil.getCurrentUser()).thenReturn(user);
        when(changeRequestService.createChangeRequest(repoId, user, view,"test-log-id"))
                .thenReturn(cr);

        ResponseEntity<UUID> response =
                changeRequestController.createChangeRequest(repoId, view, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(crId, response.getBody());

        verify(changeRequestService).createChangeRequest(repoId, user, view,"test-log-id");
    }

    @Test
    void shouldAddItemToChangeRequest() {

        UUID repoId = UUID.randomUUID();
        UUID crId = UUID.randomUUID();

        HttpServletRequest request = mock(HttpServletRequest.class);

        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());

        ItemInView view = mock(ItemInView.class);
        MultipartFile file = mock(MultipartFile.class);

        when(userContextUtil.getCurrentUser()).thenReturn(user);

        doNothing().when(changeRequestService).addItemToChangeRequest(
                eq(repoId),
                eq(user),
                eq(crId),
                anyList(),
                anyList(),
                any()
        );

        ResponseEntity<Map<String, String>> response =
                changeRequestController.addItems(
                        repoId,
                        crId,
                        List.of(view),
                        List.of(file),
                        request
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(changeRequestService).addItemToChangeRequest(
                eq(repoId),
                eq(user),
                eq(crId),
                anyList(),
                anyList(),
                any()
        );
    }

    @Test
    void shouldApproveChangeRequest() {

        UUID repoId = UUID.randomUUID();
        UUID crId = UUID.randomUUID();

        HttpServletRequest request = mock(HttpServletRequest.class);

        AppUser user = new AppUser();

        when(userContextUtil.getCurrentUser()).thenReturn(user);

        ResponseEntity<String> response =
                changeRequestController.approveChangeRequest(repoId, crId, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Change request approved", response.getBody());

        verify(userContextUtil).getCurrentUser();
        verify(changeRequestService)
                .approveChangeRequest(repoId, crId, user,"test-log-id");
        verify(request).setAttribute(eq("logId"), any());
    }

    @Test
    void shouldRejectChangeRequest() {

        UUID repoId = UUID.randomUUID();
        UUID crId = UUID.randomUUID();

        HttpServletRequest request = mock(HttpServletRequest.class);

        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());

        when(userContextUtil.getCurrentUser()).thenReturn(user);

        ResponseEntity<String> response =
                changeRequestController.rejectChangeRequest(repoId, crId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Change request rejected", response.getBody());

        verify(changeRequestService).rejectChangeRequest(repoId, user, crId,"test-log-id");
    }
}