package com.abhishek.task_management.repository;

import com.abhishek.task_management.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AttachmentRepository
        extends JpaRepository<Attachment, UUID> {

    List<Attachment> findByTaskIdOrderByCreatedAtAsc(
            UUID taskId
    );
}