package com.xiangmuguanli.controller;

import com.xiangmuguanli.dto.response.ApiResponse;
import com.xiangmuguanli.dto.response.TaskResponse;
import com.xiangmuguanli.entity.Task;
import com.xiangmuguanli.enums.TaskStatus;
import com.xiangmuguanli.service.ProjectService;
import com.xiangmuguanli.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;
    private final ProjectService projectService;

    public TaskController(TaskService taskService, ProjectService projectService) {
        this.taskService = taskService;
        this.projectService = projectService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getTasksByProject(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long projectId, Pageable pageable) {
        if (!projectService.checkProjectAccess(projectId, userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(403, "You don't have access to this project"));
        }
        Page<TaskResponse> tasks = taskService.getTasksByProject(projectId, pageable);
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @GetMapping("/assignee/{userId}")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getTasksByAssignee(
            @PathVariable Long userId, Pageable pageable) {
        Page<TaskResponse> tasks = taskService.getTasksByAssignee(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getTasksByStatus(
            @PathVariable TaskStatus status, Pageable pageable) {
        Page<TaskResponse> tasks = taskService.getTasksByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @GetMapping("/due")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getTasksByDueDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Pageable pageable) {
        Page<TaskResponse> tasks = taskService.getTasksByDueDateBetween(start, end, pageable);
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@taskService.checkTaskAccess(#id, authentication.name)")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(@PathVariable Long id) {
        TaskResponse task = taskService.getTaskById(id);
        return ResponseEntity.ok(ApiResponse.success(task));
    }

    @PostMapping
    @PreAuthorize("@projectService.checkProjectMember(#projectId, authentication.name)")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @RequestParam Long projectId,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) String attachments,
            @RequestParam Long creatorId,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) Integer sortOrder) {
        TaskResponse task = taskService.createTask(projectId, title, description, assigneeId,
                priority, dueDate, startDate, tags, attachments, creatorId, parentId, sortOrder);
        return ResponseEntity.ok(ApiResponse.success(task));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@taskService.checkTaskMember(#id, authentication.name)")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) Integer sortOrder) {
        TaskResponse task = taskService.updateTask(id, title, description, assigneeId, status,
                priority, dueDate, startDate, tags, parentId, sortOrder);
        return ResponseEntity.ok(ApiResponse.success(task));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("@taskService.checkTaskMember(#id, authentication.name)")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskStatus(
            @PathVariable Long id,
            @RequestParam TaskStatus status) {
        TaskResponse task = taskService.updateTask(id, null, null, null, status,
                null, null, null, null, null, null);
        return ResponseEntity.ok(ApiResponse.success(task));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@taskService.checkTaskMember(#id, authentication.name)")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
    }

    // Subtasks

    @GetMapping("/{id}/subtasks")
    @PreAuthorize("@taskService.checkTaskAccess(#id, authentication.name)")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getSubtasks(@PathVariable Long id) {
        List<TaskResponse> subtasks = taskService.getSubtasks(id);
        return ResponseEntity.ok(ApiResponse.success(subtasks));
    }

    @PutMapping("/{id}/parent")
    @PreAuthorize("@taskService.checkTaskMember(#id, authentication.name)")
    public ResponseEntity<ApiResponse<TaskResponse>> setParentTask(
            @PathVariable Long id,
            @RequestParam(required = false) Long parentId) {
        TaskResponse task = taskService.setParentTask(id, parentId);
        return ResponseEntity.ok(ApiResponse.success(task));
    }

    // Dependencies

    @PostMapping("/{id}/dependencies")
    @PreAuthorize("@taskService.checkTaskMember(#id, authentication.name)")
    public ResponseEntity<ApiResponse<Void>> addDependency(
            @PathVariable Long id,
            @RequestParam Long dependOnTaskId) {
        taskService.addDependency(id, dependOnTaskId);
        return ResponseEntity.ok(ApiResponse.success("Dependency added", null));
    }

    @DeleteMapping("/{id}/dependencies/{dependOnTaskId}")
    @PreAuthorize("@taskService.checkTaskMember(#id, authentication.name)")
    public ResponseEntity<ApiResponse<Void>> removeDependency(
            @PathVariable Long id,
            @PathVariable Long dependOnTaskId) {
        taskService.removeDependency(id, dependOnTaskId);
        return ResponseEntity.ok(ApiResponse.success("Dependency removed", null));
    }

    // Sort order

    @PutMapping("/{id}/sort-order")
    @PreAuthorize("@taskService.checkTaskMember(#id, authentication.name)")
    public ResponseEntity<ApiResponse<Void>> updateSortOrder(
            @PathVariable Long id,
            @RequestParam Integer sortOrder) {
        taskService.updateSortOrder(id, sortOrder);
        return ResponseEntity.ok(ApiResponse.success("Sort order updated", null));
    }

    @PostMapping("/batch/sort-order")
    @PreAuthorize("@projectService.checkProjectMember(#projectId, authentication.name)")
    public ResponseEntity<ApiResponse<Void>> batchUpdateSortOrder(
            @RequestParam Long projectId,
            @RequestBody List<Long> taskIds) {
        taskService.batchUpdateSortOrder(taskIds);
        return ResponseEntity.ok(ApiResponse.success("Sort order updated", null));
    }

    @GetMapping("/project/{projectId}/status/{status}/sorted")
    @PreAuthorize("@projectService.checkProjectAccess(#projectId, authentication.name)")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByProjectAndStatusSorted(
            @PathVariable Long projectId,
            @PathVariable TaskStatus status) {
        List<TaskResponse> tasks = taskService.getTasksByProjectAndStatusSorted(projectId, status);
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }
}
