package com.abhishek.task_management.repository;

import com.abhishek.task_management.entity.Task;
import com.abhishek.task_management.entity.TaskPriority;
import com.abhishek.task_management.entity.TaskStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaskSpecification {

    public static Specification<Task> filter(
            UUID userId,
            TaskStatus status,
            TaskPriority priority,
            UUID projectId,
            UUID assignedTo,
            String search
    ) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            Predicate creator =
                    criteriaBuilder.equal(
                            root.get("createdBy").get("id"),
                            userId
                    );

            Predicate assignee =
                    criteriaBuilder.equal(
                            root.get("assignedTo").get("id"),
                            userId
                    );

            var subquery = query.subquery(UUID.class);

            var projectMemberRoot =
                    subquery.from(
                            com.abhishek.task_management.entity.ProjectMember.class
                    );

            subquery.select(projectMemberRoot.get("id"))
                    .where(
                            criteriaBuilder.equal(
                                    projectMemberRoot.get("project"),
                                    root.get("project")
                            ),
                            criteriaBuilder.equal(
                                    projectMemberRoot.get("user").get("id"),
                                    userId
                            )
                    );

            Predicate projectMember = criteriaBuilder.exists(subquery);

            predicates.add(
                    criteriaBuilder.or(
                            creator,
                            assignee,
                            projectMember
                    )
            );

            if (status != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("status"),
                                status
                        )
                );
            }

            if (priority != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("priority"),
                                priority
                        )
                );
            }

            if (projectId != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("project").get("id"),
                                projectId
                        )
                );
            }

            if (assignedTo != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("assignedTo").get("id"),
                                assignedTo
                        )
                );
            }

            if (search != null && !search.isBlank()) {

                String searchPattern =
                        "%" + search.toLowerCase() + "%";

                Predicate titleMatch =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.get("title")
                                ),
                                searchPattern
                        );

                Predicate descriptionMatch =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.get("description")
                                ),
                                searchPattern
                        );

                predicates.add(
                        criteriaBuilder.or(
                                titleMatch,
                                descriptionMatch
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}