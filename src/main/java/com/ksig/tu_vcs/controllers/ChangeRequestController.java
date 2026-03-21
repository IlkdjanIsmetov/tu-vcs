package com.ksig.tu_vcs.controllers;

import com.ksig.tu_vcs.repos.ChangeRequestItemRepository;
import com.ksig.tu_vcs.repos.entities.AppUser;
import com.ksig.tu_vcs.repos.entities.ChangeRequest;
import com.ksig.tu_vcs.repos.entities.ChangeRequestItem;
import com.ksig.tu_vcs.services.ChangeRequestService;
import com.ksig.tu_vcs.services.views.CreateCRView;
import com.ksig.tu_vcs.services.views.ItemInView;
import com.ksig.tu_vcs.utils.UserContextUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
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
    public ResponseEntity<ChangeRequest> createChangeRequest(@PathVariable("repositoryId") UUID repositoryId, @RequestBody CreateCRView view) {
        AppUser currentUser = userContextUtil.getCurrentUser();

        ChangeRequest changeRequest = changeRequestService.createChangeRequest(repositoryId, currentUser.getId(), view);
        return ResponseEntity.ok(changeRequest);
    }

    @PostMapping("/{changeRequestId}/item")
    public ResponseEntity<ChangeRequestItem> addItem(@PathVariable ("changeRequestId") UUID changeRequestId, @RequestBody ItemInView view) {
        ChangeRequestItem item = changeRequestService.addItemToChangeRequest(changeRequestId, view);

        return ResponseEntity.ok(item);
    }

    @PostMapping("/{changeRequestId}/approve")
    public ResponseEntity<String> approveChangeRequest(@PathVariable("repositoryId") UUID repositoryId, @PathVariable ("changeRequestId") UUID changeRequestId,
                                                       @RequestPart("paths") List<ItemInView> items, @RequestPart("files") List<MultipartFile> files,
                                                       @RequestParam("message") String message){
        AppUser currentUser = userContextUtil.getCurrentUser();

        changeRequestService.approveChangeRequest(repositoryId,changeRequestId,items,files,message,currentUser);

        return ResponseEntity.ok("Change request approved");
    }

    @PostMapping("/{changeRequestId}/reject")
    public ResponseEntity<String> rejectChangeRequest(@PathVariable UUID changeRequestId){
        changeRequestService.rejectChangeRequest(changeRequestId);

        return ResponseEntity.ok("Change request rejected");
    }

}
