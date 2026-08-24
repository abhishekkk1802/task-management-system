package com.abhishek.task_management.entity;


import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "project_members",
        uniqueConstraints = @UniqueConstraint(
               columnNames = {"project_id","user_id"}
        )
)
public class ProjectMember {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectRole role;

    private LocalDateTime joinedDate;

    protected ProjectMember() {
        // JPA
    }

    public ProjectMember(
            Project project,
            User user,
            ProjectRole role
    ){
        this.project = project;
        this.user = user;
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Project getProject() {
        return project;
    }

    public ProjectRole getRole() {
        return role;
    }

    public LocalDateTime getJoinedDate() {
        return joinedDate;
    }
}
