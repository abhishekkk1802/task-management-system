package com.abhishek.task_management.service;

import com.abhishek.task_management.dto.CommentResponse;
import com.abhishek.task_management.dto.CreateCommentRequest;
import com.abhishek.task_management.dto.UpdateCommentRequest;
import com.abhishek.task_management.entity.Comment;
import com.abhishek.task_management.entity.ProjectMember;
import com.abhishek.task_management.entity.ProjectRole;
import com.abhishek.task_management.entity.Task;
import com.abhishek.task_management.entity.User;
import com.abhishek.task_management.exception.ResourceNotFoundException;
import com.abhishek.task_management.repository.CommentRepository;
import com.abhishek.task_management.repository.ProjectMemberRepository;
import com.abhishek.task_management.repository.TaskRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public CommentService(
            CommentRepository commentRepository,
            TaskRepository taskRepository,
            ProjectMemberRepository projectMemberRepository
    ) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Transactional
    public CommentResponse createComment(
            UUID taskId,
            CreateCommentRequest request
    ) {

        User currentUser = getCurrentUser();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Task not found"
                        )
                );

        if (!canComment(task, currentUser.getId())) {
            throw new AccessDeniedException(
                    "You are not allowed to comment on this task"
            );
        }

        Comment comment = new Comment(
                request.content(),
                task,
                currentUser
        );

        Comment savedComment =
                commentRepository.save(comment);

        return toResponse(savedComment);
    }

    public List<CommentResponse> getComments(UUID taskId) {

        User currentUser = getCurrentUser();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Task not found"
                        )
                );

        if (!canComment(task, currentUser.getId())) {
            throw new AccessDeniedException(
                    "You are not allowed to view comments on this task"
            );
        }

        return commentRepository
                .findByTaskIdOrderByCreatedAtAsc(taskId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CommentResponse updateComment(
            UUID commentId,
            UpdateCommentRequest request
    ) {

        User currentUser = getCurrentUser();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Comment not found"
                        )
                );

        if (!canModifyComment(comment, currentUser.getId())) {
            throw new AccessDeniedException(
                    "You are not allowed to update this comment"
            );
        }

        comment.setContent(request.content());

        Comment savedComment =
                commentRepository.save(comment);

        return toResponse(savedComment);
    }

    @Transactional
    public void deleteComment(UUID commentId) {

        User currentUser = getCurrentUser();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Comment not found"
                        )
                );

        if (!canModifyComment(comment, currentUser.getId())) {
            throw new AccessDeniedException(
                    "You are not allowed to delete this comment"
            );
        }

        commentRepository.delete(comment);
    }

    private boolean canComment(
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

    private boolean canModifyComment(
            Comment comment,
            UUID userId
    ) {

        // Comment creator
        if (comment.getCreatedBy()
                .getId()
                .equals(userId)) {
            return true;
        }

        // Project owner
        Task task = comment.getTask();

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

    private CommentResponse toResponse(
            Comment comment
    ) {

        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getTask().getId(),
                comment.getCreatedBy().getId(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}