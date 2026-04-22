package com.ksig.tu_vcs.controllers;

import com.ksig.tu_vcs.repos.entities.AppUser;
import com.ksig.tu_vcs.repos.entities.ChangeRequest;
import com.ksig.tu_vcs.services.ChangeRequestService;
import com.ksig.tu_vcs.services.views.ChangeRequestOutView;
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

@RestController
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
        ChangeRequest changeRequest = changeRequestService.createChangeRequest(repositoryId, currentUser, view);
        return ResponseEntity.ok(changeRequest.getId());
    }

    @PostMapping(value = "/{changeRequestId}/items", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, String>> addItems(@PathVariable UUID repositoryId,
                                                        @PathVariable("changeRequestId") UUID changeRequestId,
                                                        @RequestPart("paths") List<ItemInView> views,
                                                        @RequestPart("files") List<MultipartFile> files,
                                                        HttpServletRequest request) {
        AppUser currentUser = userContextUtil.getCurrentUser();
        String logId = UUID.randomUUID().toString();
        request.setAttribute("logId", logId);
        changeRequestService.addItemToChangeRequest(repositoryId, currentUser,changeRequestId, views, files, logId);
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
    public ResponseEntity<String> rejectChangeRequest(@PathVariable("repositoryId") UUID repositoryId,
                                                      @PathVariable("changeRequestId") UUID changeRequestId,
                                                      HttpServletRequest request) {
        AppUser currentUser = userContextUtil.getCurrentUser();
        String logId = UUID.randomUUID().toString();
        request.setAttribute("logId", logId);
        changeRequestService.rejectChangeRequest(repositoryId, currentUser, changeRequestId);
        return ResponseEntity.ok("Change request rejected");
    }

    @GetMapping("/pending/requests")
    public ResponseEntity<List<ChangeRequestOutView>> getPendingRequests(@PathVariable("repositoryId") UUID repositoryId,HttpServletRequest request){
        String logId = UUID.randomUUID().toString();
        request.setAttribute("logId", logId);
        return ResponseEntity.ok(changeRequestService.showPendingRequests(repositoryId,logId));
    }

    @GetMapping("/pending/count")
    public  ResponseEntity<Long> getPendingCount(@PathVariable("repositoryId") UUID repositoryId){
        long count = changeRequestService.countPendingRequests(repositoryId);
        return ResponseEntity.ok(count);
    }
}