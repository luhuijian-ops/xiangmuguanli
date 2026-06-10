package com.xiangmuguanli.service;

import com.xiangmuguanli.dto.response.ActivityResponse;
import com.xiangmuguanli.entity.Activity;
import com.xiangmuguanli.entity.Project;
import com.xiangmuguanli.entity.User;
import com.xiangmuguanli.exception.ResourceNotFoundException;
import com.xiangmuguanli.repository.ActivityRepository;
import com.xiangmuguanli.repository.ProjectRepository;
import com.xiangmuguanli.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final ActivityRepository activityRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public AuditService(ActivityRepository activityRepository,
                        ProjectRepository projectRepository,
                        UserRepository userRepository) {
        this.activityRepository = activityRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Activity logActivity(Long userId, String action, String entityType,
                                 Long entityId, Long projectId, String metadata) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Activity activity = new Activity();
        activity.setUser(user);
        activity.setAction(action);
        activity.setEntityType(entityType);
        activity.setEntityId(entityId);
        activity.setMetadata(metadata);

        if (projectId != null) {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
            activity.setProject(project);
        }

        return activityRepository.save(activity);
    }

    public Page<ActivityResponse> getActivitiesByProject(Long projectId, Pageable pageable) {
        return activityRepository.findByProjectIdOrderByCreatedAtDesc(projectId, pageable)
                .map(ActivityResponse::fromEntity);
    }

    public Page<ActivityResponse> getActivitiesByUser(Long userId, Pageable pageable) {
        return activityRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(ActivityResponse::fromEntity);
    }

    public Page<ActivityResponse> getActivitiesByEntity(String entityType, Long entityId, Pageable pageable) {
        return activityRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId, pageable)
                .map(ActivityResponse::fromEntity);
    }

    public Page<ActivityResponse> getAuditLogs(String action, String startDate, String endDate, Pageable pageable) {
        return activityRepository.findAuditLogs(action, startDate, endDate, pageable)
                .map(ActivityResponse::fromEntity);
    }
}
