package com.abhishek.task_management.service;

import com.abhishek.task_management.dto.CreateTaskRequest;
import com.abhishek.task_management.dto.TaskResponse;
import com.abhishek.task_management.dto.UpdateTaskRequest;
import com.abhishek.task_management.entity.*;
import com.abhishek.task_management.exception.ResourceNotFoundException;
import com.abhishek.task_management.repository.ProjectMemberRepository;
import com.abhishek.task_management.repository.ProjectRepository;
import com.abhishek.task_management.repository.TaskRepository;
import com.abhishek.task_management.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository, UserRepository userRepository, ProjectMemberRepository projectMemberRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    public TaskResponse createTask(CreateTaskRequest request) throws AccessDeniedException {
        User createdBy = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        Project project = null;

        if (request.projectId() != null) {
            project = projectRepository
                    .findById(request.projectId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Project not found")
                    );

            projectMemberRepository.findByProjectIdAndUserId(project.getId(),createdBy.getId())
                    .orElseThrow(()->
                            new AccessDeniedException(
                                    "You are not a member of this project"
                            )
                    );

        }

        User assignedTo = null;

        if(request.assignedTo()!=null){
            assignedTo = userRepository.findById(request.assignedTo())
                    .orElseThrow(()->
                            new ResourceNotFoundException("Assigned user not found")
                    );

            if(project != null){
                projectMemberRepository.findByProjectIdAndUserId(project.getId(),assignedTo.getId())
                        .orElseThrow(()->
                                new org.springframework.security.access.AccessDeniedException("Assigned user is not the member of the project"));
            }

        }

        Task task = new Task(
                request.title(),
                request.description(),
                request.dueDate(),
                createdBy,
                assignedTo,
                project
        );

        Task savedTask = taskRepository.save(task);

        return toResponse(savedTask);
    }

    public List<TaskResponse> getTasks(){

        User currentUser = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        List<Task> tasks =
                taskRepository.findVisibleTasks(currentUser.getId());

        return tasks.stream()
                .map(this::toResponse)
                .toList();
    }

    public TaskResponse getTask(UUID taskId) throws AccessDeniedException {

        User currentUser = (User) Objects.requireNonNull(SecurityContextHolder
                        .getContext()
                        .getAuthentication())
                .getPrincipal();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Task not found")
                );

        assert currentUser != null;
        if (!canViewTask(task, currentUser.getId())) {
            throw new AccessDeniedException(
                    "You are not allowed to view this task"
            );
        }

        return toResponse(task);
    }


    public TaskResponse updateTask(
            UUID taskId,
            UpdateTaskRequest request
    ) throws AccessDeniedException {

        // 1. Get the currently logged-in user
        User currentUser = getCurrentUser();

        // 2. Find the task
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Task not found")
                );

        UUID userId = currentUser.getId();

        // 3. Check user's permissions
        boolean creator = isTaskCreator(task, userId);

        ProjectRole projectRole = getProjectRole(task, userId);

        boolean projectOwner = projectRole == ProjectRole.OWNER;

        boolean assignee = task.getAssignedTo() != null &&
                task.getAssignedTo().getId().equals(userId);

        // User must be creator, project owner, or assignee
        if (!creator && !projectOwner && !assignee) {
            throw new AccessDeniedException(
                    "You are not allowed to update this task"
            );
        }

        // --------------------------------------------------
        // 4. Determine the TARGET project
        // --------------------------------------------------

        Project targetProject = task.getProject();

        if (request.projectId() != null) {

            // Only creator or project owner can change project
            if (!creator && !projectOwner) {
                throw new AccessDeniedException(
                        "Only the task creator or project owner can change the project"
                );
            }

            targetProject = projectRepository
                    .findById(request.projectId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Project not found")
                    );
        }

        // --------------------------------------------------
        // 5. Determine the TARGET assignee
        // --------------------------------------------------

        User targetAssignee = task.getAssignedTo();

        if (request.assignedTo() != null) {

            // Only creator or project owner can reassign
            if (!creator && !projectOwner) {
                throw new AccessDeniedException(
                        "Only the task creator or project owner can reassign the task"
                );
            }

            targetAssignee = userRepository
                    .findById(request.assignedTo())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Assigned user not found")
                    );
        }

        // --------------------------------------------------
        // 6. Validate TARGET project and assignee
        // --------------------------------------------------

        if (targetProject != null) {

            // Current user must belong to the target project
            projectMemberRepository
                    .findByProjectIdAndUserId(
                            targetProject.getId(),
                            currentUser.getId()
                    )
                    .orElseThrow(() ->
                            new AccessDeniedException(
                                    "You are not a member of the project"
                            )
                    );

            // Assignee must belong to the target project
            if (targetAssignee != null) {

                projectMemberRepository
                        .findByProjectIdAndUserId(
                                targetProject.getId(),
                                targetAssignee.getId()
                        )
                        .orElseThrow(() ->
                                new AccessDeniedException(
                                        "Assigned user is not a member of the project"
                                )
                        );
            }
        }

        // --------------------------------------------------
        // 7. Update normal task fields
        // --------------------------------------------------

        if (request.title() != null) {
            task.setTitle(request.title());
        }

        if (request.description() != null) {
            task.setDescription(request.description());
        }

        if (request.status() != null) {
            task.setStatus(request.status());
        }

        if (request.priority() != null) {
            task.setPriority(request.priority());
        }

        if (request.dueDate() != null) {
            task.setDueDate(request.dueDate());
        }

        // --------------------------------------------------
        // 8. Apply target project and assignee
        // --------------------------------------------------

        if (request.projectId() != null) {
            task.setProject(targetProject);
        }

        if (request.assignedTo() != null) {
            task.setAssignedTo(targetAssignee);
        }

        // --------------------------------------------------
        // 9. Save
        // --------------------------------------------------

        Task savedTask = taskRepository.save(task);

        // --------------------------------------------------
        // 10. Return response DTO
        // --------------------------------------------------

        return toResponse(savedTask);
    }

    public void deleteTask(UUID taskId) throws AccessDeniedException {

        User currentUser = getCurrentUser();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Task not found")
                );

        UUID userId = currentUser.getId();

        boolean creator = isTaskCreator(task, userId);

        ProjectRole projectRole = getProjectRole(task, userId);

        boolean projectOwner = projectRole == ProjectRole.OWNER;

        if (!creator && !projectOwner) {
            throw new AccessDeniedException(
                    "You are not allowed to delete this task"
            );
        }



        taskRepository.delete(task);
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getCreatedBy().getId(),
                task.getAssignedTo() != null
                        ? task.getAssignedTo().getId()
                        : null,
                task.getProject() != null
                        ? task.getProject().getId()
                        : null
        );
    }

    private boolean canViewTask(Task task, UUID userId) {

        // User created the task
        if (task.getCreatedBy().getId().equals(userId)) {
            return true;
        }

        // User is assigned to the task
        if (task.getAssignedTo() != null &&
                task.getAssignedTo().getId().equals(userId)) {
            return true;
        }

        // Task has a project and user is a project member
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

    private boolean isTaskCreator(Task task, UUID userId) {
        return task.getCreatedBy().getId().equals(userId);
    }

    private ProjectRole getProjectRole(Task task, UUID userId) {

        if (task.getProject() == null) {
            return null;
        }

        return projectMemberRepository
                .findByProjectIdAndUserId(
                        task.getProject().getId(),
                        userId
                )
                .map(ProjectMember::getRole)
                .orElse(null);
    }

    private User getCurrentUser(){
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        return (User) authentication.getPrincipal();
    }







}