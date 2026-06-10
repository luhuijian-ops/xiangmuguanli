package com.xiangmuguanli.repository;

import com.xiangmuguanli.entity.Task;
import com.xiangmuguanli.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectId(Long projectId);

    Page<Task> findByProjectId(Long projectId, Pageable pageable);

    List<Task> findByAssignedUserId(Long userId);

    Page<Task> findByAssignedUserId(Long userId, Pageable pageable);

    List<Task> findByCreatedByUserId(Long userId);

    Page<Task> findByCreatedByUserId(Long userId, Pageable pageable);

    List<Task> findByStatus(TaskStatus status);

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    List<Task> findByProjectIdAndStatus(Long projectId, TaskStatus status);

    List<Task> findByDueDateBetween(LocalDate start, LocalDate end);

    Page<Task> findByDueDateBetween(LocalDate start, LocalDate end, Pageable pageable);

    List<Task> findByAssignedUserIdAndStatus(Long userId, TaskStatus status);

    List<Task> findByProjectIdAndParentTaskId(Long projectId, Long parentId);

    List<Task> findByProjectIdAndParentTaskIdOrderBySortOrderAsc(Long projectId, Long parentId);

    List<Task> findByProjectIdAndStatusOrderBySortOrderAsc(Long projectId, TaskStatus status);

    List<Task> findByParentTaskId(Long parentId);
}
