package com.xiangmuguanli.dto.response;

import com.xiangmuguanli.entity.WorkHour;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class WorkHourResponse {

    private Long id;
    private Long userId;
    private String userName;
    private Long taskId;
    private String taskTitle;
    private Long projectId;
    private String projectName;
    private BigDecimal hours;
    private LocalDate date;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static WorkHourResponse fromEntity(WorkHour workHour) {
        WorkHourResponse response = new WorkHourResponse();
        response.setId(workHour.getId());
        response.setUserId(workHour.getUser().getId());
        response.setUserName(workHour.getUser().getName());
        response.setHours(workHour.getHours());
        response.setDate(workHour.getDate());
        response.setDescription(workHour.getDescription());
        response.setCreatedAt(workHour.getCreatedAt());
        response.setUpdatedAt(workHour.getUpdatedAt());

        if (workHour.getTask() != null) {
            response.setTaskId(workHour.getTask().getId());
            response.setTaskTitle(workHour.getTask().getTitle());
        }
        if (workHour.getProject() != null) {
            response.setProjectId(workHour.getProject().getId());
            response.setProjectName(workHour.getProject().getName());
        }

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

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
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

    public BigDecimal getHours() {
        return hours;
    }

    public void setHours(BigDecimal hours) {
        this.hours = hours;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
