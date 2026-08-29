package com.abhishek.task_management.controller;

import com.abhishek.task_management.dto.AttachmentResponse;
import com.abhishek.task_management.service.AttachmentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(
            AttachmentService attachmentService
    ) {
        this.attachmentService = attachmentService;
    }

    @PostMapping("/tasks/{taskId}/attachments")
    public AttachmentResponse uploadAttachment(
            @PathVariable UUID taskId,
            @RequestParam("file") MultipartFile file
    ) {
        return attachmentService.uploadAttachment(
                taskId,
                file
        );
    }

    @GetMapping("/tasks/{taskId}/attachments")
    public List<AttachmentResponse> getAttachments(
            @PathVariable UUID taskId
    ) {
        return attachmentService.getAttachments(taskId);
    }

    @DeleteMapping("/attachments/{attachmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAttachment(
            @PathVariable UUID attachmentId
    ) {
        attachmentService.deleteAttachment(
                attachmentId
        );
    }
}