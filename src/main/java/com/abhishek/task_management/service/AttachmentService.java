package com.abhishek.task_management.service;

import com.abhishek.task_management.dto.AttachmentResponse;
import com.abhishek.task_management.entity.Attachment;
import com.abhishek.task_management.entity.ProjectMember;
import com.abhishek.task_management.entity.ProjectRole;
import com.abhishek.task_management.entity.Task;
import com.abhishek.task_management.entity.User;
import com.abhishek.task_management.exception.ResourceNotFoundException;
import com.abhishek.task_management.repository.AttachmentRepository;
import com.abhishek.task_management.repository.ProjectMemberRepository;
import com.abhishek.task_management.repository.TaskRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final FileStorageService fileStorageService;

    public AttachmentService(
            AttachmentRepository attachmentRepository,
            TaskRepository taskRepository,
            ProjectMemberRepository projectMemberRepository,
            FileStorageService fileStorageService
    ) {
        this.attachmentRepository = attachmentRepository;
        this.taskRepository = taskRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public AttachmentResponse uploadAttachment(
            UUID taskId,
            MultipartFile file
    ) {

        User currentUser = getCurrentUser();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Task not found"
                        )
                );

        if (!canAccessTask(task, currentUser.getId())) {
            throw new AccessDeniedException(
                    "You are not allowed to add attachments to this task"
            );
        }

        String filePath = fileStorageService.store(file);

        try {

            Attachment attachment = new Attachment(
                    file.getOriginalFilename(),
                    filePath,
                    file.getContentType(),
                    file.getSize(),
                    task,
                    currentUser
            );

            return toResponse(
                    attachmentRepository.save(attachment)
            );

        } catch (Exception e) {

            fileStorageService.delete(filePath);

            throw e;
        }
    }

    public List<AttachmentResponse> getAttachments(
            UUID taskId
    ) {

        User currentUser = getCurrentUser();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Task not found"
                        )
                );

        if (!canAccessTask(task, currentUser.getId())) {
            throw new AccessDeniedException(
                    "You are not allowed to view attachments"
            );
        }

        return attachmentRepository
                .findByTaskIdOrderByCreatedAtAsc(taskId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deleteAttachment(
            UUID attachmentId
    ) {

        User currentUser = getCurrentUser();

        Attachment attachment =
                attachmentRepository.findById(attachmentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Attachment not found"
                                )
                        );

        if (!canDeleteAttachment(
                attachment,
                currentUser.getId()
        )) {
            throw new AccessDeniedException(
                    "You are not allowed to delete this attachment"
            );
        }

        fileStorageService.delete(
                attachment.getFilePath()
        );

        attachmentRepository.delete(attachment);
    }

    private boolean canAccessTask(
            Task task,
            UUID userId
    ) {

        // Task creator
        if (task.getCreatedBy()
                .getId()
                .equals(userId)) {
            return true;
        }

        // Task assignee
        if (task.getAssignedTo() != null &&
                task.getAssignedTo()
                        .getId()
                        .equals(userId)) {
            return true;
        }

        // Project member
        if (task.getProject() != null) {

            return projectMemberRepository
                    .findByProjectIdAndUserId(
                            task.getProject().getId(),
                            userId
                    )
                    .isPresent();
        }

        return false;
    }

    private boolean canDeleteAttachment(
            Attachment attachment,
            UUID userId
    ) {

        // Uploader can delete
        if (attachment.getUploadedBy()
                .getId()
                .equals(userId)) {
            return true;
        }

        // Project owner can delete
        Task task = attachment.getTask();

        if (task.getProject() == null) {
            return false;
        }

        ProjectMember member =
                projectMemberRepository
                        .findByProjectIdAndUserId(
                                task.getProject().getId(),
                                userId
                        )
                        .orElse(null);

        return member != null &&
                member.getRole() == ProjectRole.OWNER;
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        return (User) authentication.getPrincipal();
    }

    private AttachmentResponse toResponse(
            Attachment attachment
    ) {

        return new AttachmentResponse(
                attachment.getId(),
                attachment.getFileName(),
                attachment.getContentType(),
                attachment.getFileSize(),
                attachment.getTask().getId(),
                attachment.getUploadedBy().getId(),
                attachment.getCreatedAt()
        );
    }
}