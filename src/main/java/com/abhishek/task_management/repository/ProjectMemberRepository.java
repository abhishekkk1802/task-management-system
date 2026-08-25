package com.abhishek.task_management.repository;

import com.abhishek.task_management.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {
    Optional<ProjectMember> findByProjectIdAndUserId(UUID projectId,UUID userId);

    void deleteAllByProjectId(UUID projectId);

    List<ProjectMember> findByUserId(UUID id);
}
