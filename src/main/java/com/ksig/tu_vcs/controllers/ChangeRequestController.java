package com.ksig.tu_vcs.controllers;

import com.ksig.tu_vcs.repos.ChangeRequestItemRepository;
import com.ksig.tu_vcs.repos.entities.AppUser;
import com.ksig.tu_vcs.repos.entities.ChangeRequest;
import com.ksig.tu_vcs.repos.entities.ChangeRequestItem;
import com.ksig.tu_vcs.services.ChangeRequestService;
import com.ksig.tu_vcs.services.views.CreateCRView;
import com.ksig.tu_vcs.services.views.ItemInView;
import com.ksig.tu_vcs.utils.UserContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
    @RequestMapping("/api/repositories/{repositoryId}/change-request")
public class ChangeRequestController {
    private final ChangeRequestService changeRequestService;
    private final UserContextUtil userContextUtil;


    public ChangeRequestController(ChangeRequestService changeRequestService, UserContextUtil userContextUtil) {
        this.changeRequestService = changeRequestService;
        this.userContextUtil = userContextUtil;
    }

    @PostMapping
    public ResponseEntity<UUID> createChangeRequest(@PathVariable("repositoryId") UUID repositoryId,
                                                             @RequestBody CreateCRView view,
                                                             HttpServletRequest request) {
        String logId = UUID.randomUUID().toString();
        request.setAttribute("logId", logId);
        AppUser currentUser = userContextUtil.getCurrentUser();
        ChangeRequest changeRequest = changeRequestService.createChangeRequest(repositoryId, currentUser.getId(), view);
        return ResponseEntity.ok(changeRequest.getId());
    }

    @PostMapping(value = "/{changeRequestId}/items", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, String>> addItems(@PathVariable("changeRequestId") UUID changeRequestId,
                                                       @RequestPart("paths") List<ItemInView> views,
                                                       @RequestPart("files") List<MultipartFile> files,
                                                       HttpServletRequest request) {

        String logId = UUID.randomUUID().toString();
        request.setAttribute("logId", logId);
        changeRequestService.addItemToChangeRequest(changeRequestId, views, files, logId);
        return ResponseEntity.ok(Map.of("status", "OK"));
    }

    @PostMapping("/{changeRequestId}/approve")
    public ResponseEntity<String> approveChangeRequest(@PathVariable("repositoryId") UUID repositoryId,
                                                       @PathVariable("changeRequestId") UUID changeRequestId,
                                                       HttpServletRequest request) {
        AppUser currentUser = userContextUtil.getCurrentUser();

        String logId = UUID.randomUUID().toString();
        request.setAttribute("logId", logId);
        changeRequestService.approveChangeRequest(repositoryId, changeRequestId, currentUser);
        return ResponseEntity.ok("Change request approved");
    }

    @PostMapping("/{changeRequestId}/reject")
    public ResponseEntity<String> rejectChangeRequest(@PathVariable UUID changeRequestId,
                                                      HttpServletRequest request) {

        String logId = UUID.randomUUID().toString();
        request.setAttribute("logId", logId);
        changeRequestService.rejectChangeRequest(changeRequestId);
        return ResponseEntity.ok("Change request rejected");
    }

}
