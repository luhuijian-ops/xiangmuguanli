package com.xiangmuguanli.dto.response;

import com.xiangmuguanli.entity.Alert;
import com.xiangmuguanli.enums.AlertType;
import com.xiangmuguanli.enums.Severity;

import java.time.LocalDateTime;

public class AlertResponse {

    private Long id;
    private AlertType type;
    private Severity severity;
    private Long userId;
    private String userName;
    private Long targetId;
    private String targetType;
    private String message;
    private String metadata;
    private Boolean resolved;
    private LocalDateTime createdAt;

    public static AlertResponse fromEntity(Alert alert) {
        AlertResponse response = new AlertResponse();
        response.setId(alert.getId());
        response.setType(alert.getType());
        response.setSeverity(alert.getSeverity());
        response.setUserId(alert.getUser() != null ? alert.getUser().getId() : null);
        response.setUserName(alert.getUser() != null ? alert.getUser().getName() : null);
        response.setTargetId(alert.getTargetId());
        response.setTargetType(alert.getTargetType());
        response.setMessage(alert.getMessage());
        response.setMetadata(alert.getMetadata());
        response.setResolved(alert.getResolved());
        response.setCreatedAt(alert.getCreatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AlertType getType() {
        return type;
    }

    public void setType(AlertType type) {
        this.type = type;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
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

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Boolean getResolved() {
        return resolved;
    }

    public void setResolved(Boolean resolved) {
        this.resolved = resolved;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
