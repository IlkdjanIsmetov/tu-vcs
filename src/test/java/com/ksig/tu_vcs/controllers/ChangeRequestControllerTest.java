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

        when(userContextUtil.getCurrentUser()).thenReturn(user);
        when(changeRequestService.createChangeRequest(repoId, user.getId(), view))
                .thenReturn(cr);

        ResponseEntity<ChangeRequest> response =
                changeRequestController.createChangeRequest(repoId, view, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(cr, response.getBody());

        verify(userContextUtil).getCurrentUser();
        verify(changeRequestService).createChangeRequest(repoId, user.getId(), view);
        verify(request).setAttribute(eq("logId"), any());
    }

    @Test
    void shouldAddItemToChangeRequest() {

        UUID crId = UUID.randomUUID();

        HttpServletRequest request = mock(HttpServletRequest.class);

        ItemInView view = mock(ItemInView.class);
        MultipartFile file = mock(MultipartFile.class);

        ChangeRequestItem item = new ChangeRequestItem();

        when(changeRequestService.addItemToChangeRequest(eq(crId), eq(view), eq(file), any()))
                .thenReturn(item);

        ResponseEntity<ChangeRequestItem> response =
                changeRequestController.addItem(crId, view, file, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(item, response.getBody());

        verify(changeRequestService)
                .addItemToChangeRequest(eq(crId), eq(view), eq(file), any());
        verify(request).setAttribute(eq("logId"), any());
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
                .approveChangeRequest(repoId, crId, user);
        verify(request).setAttribute(eq("logId"), any());
    }

    @Test
    void shouldRejectChangeRequest() {

        UUID crId = UUID.randomUUID();

        HttpServletRequest request = mock(HttpServletRequest.class);

        ResponseEntity<String> response =
                changeRequestController.rejectChangeRequest(crId, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Change request rejected", response.getBody());

        verify(changeRequestService).rejectChangeRequest(crId);
        verify(request).setAttribute(eq("logId"), any());
    }
}