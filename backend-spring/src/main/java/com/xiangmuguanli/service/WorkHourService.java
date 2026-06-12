package com.xiangmuguanli.service;

import com.xiangmuguanli.dto.response.WorkHourResponse;
import com.xiangmuguanli.entity.Project;
import com.xiangmuguanli.entity.Task;
import com.xiangmuguanli.entity.User;
import com.xiangmuguanli.entity.WorkHour;
import com.xiangmuguanli.exception.ResourceNotFoundException;
import com.xiangmuguanli.repository.ProjectRepository;
import com.xiangmuguanli.repository.TaskRepository;
import com.xiangmuguanli.repository.UserRepository;
import com.xiangmuguanli.repository.WorkHourRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkHourService {

    private final WorkHourRepository workHourRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final ProjectService projectService;

    public WorkHourService(WorkHourRepository workHourRepository,
                           UserRepository userRepository,
                           ProjectRepository projectRepository,
                           TaskRepository taskRepository,
                           ProjectService projectService) {
        this.workHourRepository = workHourRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.projectService = projectService;
    }

    @Transactional
    public WorkHourResponse createWorkHour(String username, Long projectId, Long taskId,
                                            BigDecimal hours, LocalDate date, String description) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        WorkHour workHour = new WorkHour();
        workHour.setUser(user);
        workHour.setHours(hours);
        workHour.setDate(date);
        workHour.setDescription(description);

        if (projectId != null) {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
            workHour.setProject(project);
        }

        if (taskId != null) {
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
            workHour.setTask(task);
        }

        workHour = workHourRepository.save(workHour);
        return WorkHourResponse.fromEntity(workHour);
    }

    public List<WorkHourResponse> getWorkHoursByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return workHourRepository.findByUserIdAndDateBetween(userId, startDate, endDate)
                .stream()
                .map(WorkHourResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<WorkHourResponse> getWorkHoursByProject(Long projectId) {
        return workHourRepository.findByProjectId(projectId)
                .stream()
                .map(WorkHourResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<WorkHourResponse> getWorkHoursByTask(Long taskId) {
        return workHourRepository.findByTaskId(taskId)
                .stream()
                .map(WorkHourResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public WorkHourResponse updateWorkHour(Long id, BigDecimal hours, LocalDate date, String description) {
        WorkHour workHour = workHourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkHour", "id", id));

        if (hours != null) {
            workHour.setHours(hours);
        }
        if (date != null) {
            workHour.setDate(date);
        }
        if (description != null) {
            workHour.setDescription(description);
        }

        workHour = workHourRepository.save(workHour);
        return WorkHourResponse.fromEntity(workHour);
    }

    @Transactional
    public void deleteWorkHour(Long id) {
        WorkHour workHour = workHourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkHour", "id", id));
        workHourRepository.delete(workHour);
    }

    // SpEL-compatible permission helpers for @PreAuthorize

    public boolean isSelfOrAdmin(Long userId, String username) {
        User currentUser = userRepository.findByUsername(username).orElse(null);
        if (currentUser == null) return false;
        if (currentUser.isAdmin()) return true;
        return currentUser.getId().equals(userId);
    }

    public boolean isWorkHourOwnerOrAdmin(Long workHourId, String username) {
        WorkHour workHour = workHourRepository.findById(workHourId).orElse(null);
        if (workHour == null) return false;
        User currentUser = userRepository.findByUsername(username).orElse(null);
        if (currentUser == null) return false;
        if (currentUser.isAdmin()) return true;
        return workHour.getUser().getId().equals(currentUser.getId());
    }
}
