package com.abhishek.task_management.controller;

import com.abhishek.task_management.dto.CommentResponse;
import com.abhishek.task_management.dto.CreateCommentRequest;
import com.abhishek.task_management.dto.UpdateCommentRequest;
import com.abhishek.task_management.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/tasks/{taskId}/comments")
    public CommentResponse createComment(
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        return commentService.createComment(taskId, request);
    }

    @GetMapping("/tasks/{taskId}/comments")
    public List<CommentResponse> getComments(
            @PathVariable UUID taskId
    ) {
        return commentService.getComments(taskId);
    }

    @PutMapping("/comments/{commentId}")
    public CommentResponse updateComment(
            @PathVariable UUID commentId,
            @Valid @RequestBody UpdateCommentRequest request
    ) {
        return commentService.updateComment(commentId, request);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable UUID commentId
    ) {
        commentService.deleteComment(commentId);
    }
}