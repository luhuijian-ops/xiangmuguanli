package com.xiangmuguanli.service;

import com.xiangmuguanli.dto.response.TaskResponse;
import com.xiangmuguanli.entity.Project;
import com.xiangmuguanli.entity.Task;
import com.xiangmuguanli.entity.TaskDependency;
import com.xiangmuguanli.entity.User;
import com.xiangmuguanli.enums.TaskStatus;
import com.xiangmuguanli.exception.ForbiddenException;
import com.xiangmuguanli.exception.BadRequestException;
import com.xiangmuguanli.exception.ResourceNotFoundException;
import com.xiangmuguanli.repository.ProjectRepository;
import com.xiangmuguanli.repository.TaskDependencyRepository;
import com.xiangmuguanli.repository.TaskRepository;
import com.xiangmuguanli.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskDependencyRepository taskDependencyRepository;
    private final ProjectService projectService;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       UserRepository userRepository,
                       TaskDependencyRepository taskDependencyRepository,
                       ProjectService projectService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.taskDependencyRepository = taskDependencyRepository;
        this.projectService = projectService;
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasksByProject(Long projectId, Pageable pageable) {
        return taskRepository.findByProjectId(projectId, pageable).map(TaskResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasksByAssignee(Long userId, Pageable pageable) {
        return taskRepository.findByAssignedUserId(userId, pageable).map(TaskResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasksByStatus(TaskStatus status, Pageable pageable) {
        return taskRepository.findByStatus(status, pageable).map(TaskResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasksByDueDateBetween(LocalDate start, LocalDate end, Pageable pageable) {
        return taskRepository.findByDueDateBetween(start, end, pageable).map(TaskResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        TaskResponse response = TaskResponse.fromEntity(task);
        List<Long> depIds = taskDependencyRepository.findByTaskId(id).stream()
                .map(d -> d.getDependOnTask().getId())
                .toList();
        response.setDependencyIds(depIds);
        return response;
    }

    @Transactional
    public TaskResponse createTask(Long projectId, String title, String description,
                                    Long assigneeId, Integer priority,
                                    LocalDate dueDate, LocalDate startDate,
                                    String tags, String attachments, Long creatorId,
                                    Long parentId, Integer sortOrder) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", creatorId));

        User assignee = null;
        if (assigneeId != null) {
            assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", assigneeId));
        }

        Task task = new Task();
        task.setProject(project);
        task.setTitle(title);
        task.setDescription(description);
        task.setAssignedUser(assignee);
        task.setCreatedByUser(creator);
        task.setPriority(priority != null ? priority : 3);
        task.setDueDate(dueDate);
        task.setStartDate(startDate);
        task.setTags(tags);
        task.setAttachments(attachments);
        task.setStatus(TaskStatus.TODO);
        task.setSortOrder(sortOrder != null ? sortOrder : 0);

        if (parentId != null) {
            Task parent = taskRepository.findById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Task", "id", parentId));
            if (!parent.getProject().getId().equals(projectId)) {
                throw new BadRequestException("Parent task must belong to the same project");
            }
            task.setParentTask(parent);
        }

        task = taskRepository.save(task);
        return TaskResponse.fromEntity(task);
    }

    @Transactional
    public TaskResponse updateTask(Long id, String title, String description,
                                   Long assigneeId, TaskStatus status, Integer priority,
                                   LocalDate dueDate, LocalDate startDate, String tags,
                                   Long parentId, Integer sortOrder) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        if (title != null && !title.isBlank()) {
            task.setTitle(title);
        }
        if (description != null) {
            task.setDescription(description);
        }
        if (assigneeId != null) {
            User assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", assigneeId));
            task.setAssignedUser(assignee);
        }
        if (status != null) {
            task.setStatus(status);
        }
        if (priority != null) {
            task.setPriority(priority);
        }
        if (dueDate != null) {
            task.setDueDate(dueDate);
        }
        if (startDate != null) {
            task.setStartDate(startDate);
        }
        if (tags != null) {
            task.setTags(tags);
        }
        if (sortOrder != null) {
            task.setSortOrder(sortOrder);
        }
        if (parentId != null) {
            if (parentId.equals(id)) {
                throw new BadRequestException("Task cannot be its own parent");
            }
            Task parent = taskRepository.findById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Task", "id", parentId));
            if (!parent.getProject().getId().equals(task.getProject().getId())) {
                throw new BadRequestException("Parent task must belong to the same project");
            }
            // Check for circular reference
            if (isDescendant(parentId, id)) {
                throw new BadRequestException("Cannot set a descendant task as parent");
            }
            task.setParentTask(parent);
        } else if (parentId == null && task.getParentTask() != null) {
            task.setParentTask(null);
        }

        task = taskRepository.save(task);
        return TaskResponse.fromEntity(task);
    }

    @Transactional
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        task.setStatus(TaskStatus.ARCHIVED);
        taskRepository.save(task);
    }

    public List<Task> getTasksByProjectId(Long projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    public List<Task> getTasksByAssigneeId(Long userId) {
        return taskRepository.findByAssignedUserId(userId);
    }

    // Parent-child tasks

    @Transactional(readOnly = true)
    public List<TaskResponse> getSubtasks(Long parentId) {
        return taskRepository.findByParentTaskId(parentId).stream()
                .map(TaskResponse::fromEntity)
                .toList();
    }

    @Transactional
    public TaskResponse setParentTask(Long taskId, Long parentId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        if (parentId == null) {
            task.setParentTask(null);
        } else {
            if (parentId.equals(taskId)) {
                throw new BadRequestException("Task cannot be its own parent");
            }
            Task parent = taskRepository.findById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Task", "id", parentId));
            if (!parent.getProject().getId().equals(task.getProject().getId())) {
                throw new BadRequestException("Parent task must belong to the same project");
            }
            if (isDescendant(parentId, taskId)) {
                throw new BadRequestException("Cannot set a descendant task as parent");
            }
            task.setParentTask(parent);
        }
        task = taskRepository.save(task);
        return TaskResponse.fromEntity(task);
    }

    private boolean isDescendant(Long ancestorId, Long candidateId) {
        List<Task> children = taskRepository.findByParentTaskId(ancestorId);
        for (Task child : children) {
            if (child.getId().equals(candidateId)) {
                return true;
            }
            if (isDescendant(child.getId(), candidateId)) {
                return true;
            }
        }
        return false;
    }

    // Task dependencies

    @Transactional
    public void addDependency(Long taskId, Long dependOnTaskId) {
        if (taskId.equals(dependOnTaskId)) {
            throw new BadRequestException("Task cannot depend on itself");
        }
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        Task dependOn = taskRepository.findById(dependOnTaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", dependOnTaskId));
        if (!task.getProject().getId().equals(dependOn.getProject().getId())) {
            throw new BadRequestException("Dependency tasks must belong to the same project");
        }
        if (taskDependencyRepository.existsByTaskIdAndDependOnTaskId(taskId, dependOnTaskId)) {
            throw new BadRequestException("Dependency already exists");
        }
        // Check circular dependency
        if (hasCircularDependency(taskId, dependOnTaskId)) {
            throw new BadRequestException("Circular dependency detected");
        }
        TaskDependency dep = new TaskDependency();
        dep.setTask(task);
        dep.setDependOnTask(dependOn);
        taskDependencyRepository.save(dep);
    }

    @Transactional
    public void removeDependency(Long taskId, Long dependOnTaskId) {
        taskDependencyRepository.deleteByTaskIdAndDependOnTaskId(taskId, dependOnTaskId);
    }

    private boolean hasCircularDependency(Long fromTaskId, Long toTaskId) {
        // If toTaskId depends (directly or transitively) on fromTaskId, it would be circular
        List<TaskDependency> deps = taskDependencyRepository.findByTaskId(toTaskId);
        for (TaskDependency dep : deps) {
            Long nextId = dep.getDependOnTask().getId();
            if (nextId.equals(fromTaskId)) {
                return true;
            }
            if (hasCircularDependency(fromTaskId, nextId)) {
                return true;
            }
        }
        return false;
    }

    public boolean areDependenciesCompleted(Long taskId) {
        List<TaskDependency> deps = taskDependencyRepository.findByTaskId(taskId);
        for (TaskDependency dep : deps) {
            if (dep.getDependOnTask().getStatus() != TaskStatus.DONE) {
                return false;
            }
        }
        return true;
    }

    // Sort order

    @Transactional
    public void updateSortOrder(Long taskId, Integer sortOrder) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        task.setSortOrder(sortOrder != null ? sortOrder : 0);
        taskRepository.save(task);
    }

    @Transactional
    public void batchUpdateSortOrder(List<Long> taskIds) {
        for (int i = 0; i < taskIds.size(); i++) {
            Task task = taskRepository.findById(taskIds.get(i)).orElse(null);
            if (task != null) {
                task.setSortOrder(i);
                taskRepository.save(task);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByProjectAndStatusSorted(Long projectId, TaskStatus status) {
        return taskRepository.findByProjectIdAndStatusOrderBySortOrderAsc(projectId, status).stream()
                .map(TaskResponse::fromEntity)
                .toList();
    }

    // SpEL-compatible permission helpers for @PreAuthorize

    public boolean checkTaskAccess(Long taskId, String username) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) return false;
        return projectService.checkProjectAccess(task.getProject().getId(), username);
    }

    public boolean checkTaskAdmin(Long taskId, String username) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) return false;
        return projectService.checkProjectAdmin(task.getProject().getId(), username);
    }

    public boolean checkTaskMember(Long taskId, String username) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) return false;
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return false;
        return projectService.isProjectMemberNotViewer(task.getProject().getId(), user.getId());
    }
}
