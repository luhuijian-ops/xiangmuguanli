package com.xiangmuguanli.dto.response;

import com.xiangmuguanli.entity.Activity;

import java.time.LocalDateTime;

public class ActivityResponse {

    private Long id;
    private Long userId;
    private String userName;
    private String action;
    private String entityType;
    private Long entityId;
    private Long projectId;
    private String projectName;
    private String metadata;
    private LocalDateTime createdAt;

    public static ActivityResponse fromEntity(Activity activity) {
        ActivityResponse response = new ActivityResponse();
        response.setId(activity.getId());
        response.setUserId(activity.getUser().getId());
        response.setUserName(activity.getUser().getName());
        response.setAction(activity.getAction());
        response.setEntityType(activity.getEntityType());
        response.setEntityId(activity.getEntityId());
        response.setProjectId(activity.getProject() != null ? activity.getProject().getId() : null);
        response.setProjectName(activity.getProject() != null ? activity.getProject().getName() : null);
        response.setMetadata(activity.getMetadata());
        response.setCreatedAt(activity.getCreatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
