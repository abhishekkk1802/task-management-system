package com.abhishek.task_management.controller;

import com.abhishek.task_management.dto.AddProjectMemberRequest;
import com.abhishek.task_management.dto.CreateProjectRequest;
import com.abhishek.task_management.dto.ProjectResponse;
import com.abhishek.task_management.dto.UpdateProjectRequest;
import com.abhishek.task_management.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {
    private final ProjectService projectService;


    public ProjectController(ProjectService projectService) {
        System.out.println("ProjectController reached");
        this.projectService = projectService;
    }


    @PostMapping
    public ProjectResponse createProject(
            @Valid @RequestBody CreateProjectRequest request
            ){
        return projectService.createProject(request);
    }

    @GetMapping("/{projectId}")
    public ProjectResponse getProject(
            @PathVariable UUID projectId
            ){

        return projectService.getProject(projectId);
    }

    @PutMapping("/{projectId}")
    public ProjectResponse updateProject(
            @PathVariable UUID projectId,
            @Valid @RequestBody UpdateProjectRequest request
    ) {
        return projectService.updateProject(projectId, request);
    }

    @PostMapping("/{projectId}/members")
    public void addMember(
            @PathVariable UUID projectId,
            @Valid @RequestBody AddProjectMemberRequest request
    ) {
        projectService.addMember(projectId, request);
    }
}
