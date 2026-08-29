package com.abhishek.task_management.repository;

import com.abhishek.task_management.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {


    @Query("""
    SELECT t
    FROM Task t
    WHERE t.createdBy.id = :userId
       OR t.assignedTo.id = :userId
       OR EXISTS (
           SELECT pm
           FROM ProjectMember pm
           WHERE pm.project = t.project
             AND pm.user.id = :userId
       )
""")
    List<Task> findVisibleTasks(@Param("userId") UUID userId);

    @Modifying
    @Query("""
    UPDATE Task t
    SET t.project = null
    WHERE t.project.id = :projectId
""")
    void detachTasksFromProject(@Param("projectId") UUID projectId);

}
