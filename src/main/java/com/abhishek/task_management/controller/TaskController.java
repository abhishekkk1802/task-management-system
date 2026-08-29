package com.abhishek.task_management.controller;

import com.abhishek.task_management.dto.CreateTaskRequest;
import com.abhishek.task_management.dto.TaskFilterRequest;
import com.abhishek.task_management.dto.TaskResponse;
import com.abhishek.task_management.dto.UpdateTaskRequest;
import com.abhishek.task_management.entity.Task;
import com.abhishek.task_management.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public TaskResponse createTask(
            @Valid @RequestBody CreateTaskRequest request
    ) throws AccessDeniedException {
        return taskService.createTask(request);
    }

//    @GetMapping
//    public List<TaskResponse> getTasks(){
//        return taskService.getTasks();
//    }

    @GetMapping
    public List<TaskResponse> getTasks(
            @ModelAttribute TaskFilterRequest request
    ) {
        return taskService.getTasks(request);
    }

    @GetMapping("/assigned-to-me")
    public List<TaskResponse> getMyTasks(
            @ModelAttribute TaskFilterRequest request
    ) {
        return taskService.getMyTasks(request);
    }

    @GetMapping("/{taskId}")
    public TaskResponse getTask(
            @PathVariable UUID taskId
    ) throws AccessDeniedException {
        return taskService.getTask(taskId);
    }

    @PutMapping("/{taskId}")
    public TaskResponse updateTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskRequest request
    ) throws AccessDeniedException {
        return taskService.updateTask(taskId, request);
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(
            @PathVariable UUID taskId
    ) throws AccessDeniedException {
        taskService.deleteTask(taskId);
    }

}