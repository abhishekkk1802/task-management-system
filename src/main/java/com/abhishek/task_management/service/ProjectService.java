package com.abhishek.task_management.service;

import com.abhishek.task_management.dto.AddProjectMemberRequest;
import com.abhishek.task_management.dto.CreateProjectRequest;
import com.abhishek.task_management.dto.ProjectResponse;
import com.abhishek.task_management.dto.UpdateProjectRequest;
import com.abhishek.task_management.entity.Project;
import com.abhishek.task_management.entity.ProjectMember;
import com.abhishek.task_management.entity.ProjectRole;
import com.abhishek.task_management.entity.User;
import com.abhishek.task_management.repository.ProjectMemberRepository;
import com.abhishek.task_management.repository.ProjectRepository;
import com.abhishek.task_management.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectMemberRepository projectMemberRepository, ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectMemberRepository = projectMemberRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request){

        System.out.println("ProjectService reached");

        User currentUser = getCurrentUser();

        System.out.println("Current user: " + currentUser.getId());

        Project project = new Project(
                request.name(),
                request.description(),
                currentUser
        );

        System.out.println("Project created");

        Project savedProject = projectRepository.save(project);

        ProjectMember projectMember = new ProjectMember(
                savedProject,
                currentUser,
                ProjectRole.OWNER
        );

        projectMemberRepository.save(projectMember);

        System.out.println("Project saved: " + savedProject.getId());

        return new ProjectResponse(
                savedProject.getId(),
                savedProject.getName(),
                savedProject.getDescription(),
                savedProject.getCreatedBy().getId(),
                savedProject.getCreatedAt(),
                savedProject.getUpdatedAt()
        );
    }

    public ProjectResponse getProject(UUID projectId){

        User currentUser = getCurrentUser();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(()->
                        new RuntimeException("Project not found")
                        );

        projectMemberRepository.findByProjectIdAndUserId(projectId,currentUser.getId())
                .orElseThrow(()->
                        new AccessDeniedException("you are not a member of this project")
                );

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedBy().getId(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    private User getCurrentUser(){
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        return (User) authentication.getPrincipal();
    }

    public ProjectResponse updateProject(
            UUID projectId,
            UpdateProjectRequest request
    ){
        User currentUser = getCurrentUser();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(()-> new RuntimeException("project not found"));

        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId,currentUser.getId())
                .orElseThrow(()-> new AccessDeniedException("you are not a member of the project"));


        if(member.getRole() != ProjectRole.OWNER){
            throw new AccessDeniedException(
                    "Only the project owner can update the project"
            );
        }

        project.setName(request.name());
        project.setDescription(request.description());

        Project savedProject = projectRepository.save(project);

        return new ProjectResponse(
                savedProject.getId(),
                savedProject.getName(),
                savedProject.getDescription(),
                savedProject.getCreatedBy().getId(),
                savedProject.getCreatedAt(),
                savedProject.getUpdatedAt()
        );
    }

    @Transactional
    public void addMember(
            UUID projectId,
            AddProjectMemberRequest request
    ) {
        User currentUser = getCurrentUser();

        // 1. Check that the project exists
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new RuntimeException("Project not found")
                );

        // 2. Check that the requester is a member
        ProjectMember requester = projectMemberRepository
                .findByProjectIdAndUserId(
                        projectId,
                        currentUser.getId()
                )
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "You are not a member of this project"
                        )
                );

        // 3. Only OWNER can add members
        if (requester.getRole() != ProjectRole.OWNER) {
            throw new AccessDeniedException(
                    "Only the project owner can add members"
            );
        }

        // 4. Find the user we're trying to add
        User userToAdd = userRepository
                .findByEmail(request.email())
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        // 5. Check if already a member
        boolean alreadyMember = projectMemberRepository
                .findByProjectIdAndUserId(
                        projectId,
                        userToAdd.getId()
                )
                .isPresent();

        if (alreadyMember) {
            throw new RuntimeException(
                    "User is already a member of this project"
            );
        }

        // 6. Add the member
        ProjectMember member = new ProjectMember(
                project,
                userToAdd,
                ProjectRole.MEMBER
        );

        projectMemberRepository.save(member);
    }

}
