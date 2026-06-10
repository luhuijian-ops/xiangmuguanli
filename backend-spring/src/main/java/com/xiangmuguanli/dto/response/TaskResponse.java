package com.xiangmuguanli.dto.response;

import com.xiangmuguanli.entity.Task;
import com.xiangmuguanli.enums.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private Long projectId;
    private String projectCode;
    private String projectName;
    private Long assignedTo;
    private String assigneeName;
    private Long createdBy;
    private String creatorName;
    private TaskStatus status;
    private Integer priority;
    private LocalDate dueDate;
    private LocalDate startDate;
    private String tags;
    private String attachments;
    private Long parentId;
    private Integer sortOrder;
    private List<Long> subtaskIds;
    private List<Long> dependencyIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TaskResponse fromEntity(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setProjectId(task.getProject().getId());
        response.setProjectCode(task.getProject().getCode());
        response.setProjectName(task.getProject().getName());
        response.setAssignedTo(task.getAssignedUser() != null ? task.getAssignedUser().getId() : null);
        response.setAssigneeName(task.getAssignedUser() != null ? task.getAssignedUser().getName() : null);
        response.setCreatedBy(task.getCreatedByUser().getId());
        response.setCreatorName(task.getCreatedByUser().getName());
        response.setStatus(task.getStatus());
        response.setPriority(task.getPriority());
        response.setDueDate(task.getDueDate());
        response.setStartDate(task.getStartDate());
        response.setTags(task.getTags());
        response.setAttachments(task.getAttachments());
        response.setParentId(task.getParentTask() != null ? task.getParentTask().getId() : null);
        response.setSortOrder(task.getSortOrder());
        if (task.getSubtasks() != null) {
            response.setSubtaskIds(task.getSubtasks().stream().map(Task::getId).toList());
        }
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Long getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Long assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
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

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public List<Long> getSubtaskIds() {
        return subtaskIds;
    }

    public void setSubtaskIds(List<Long> subtaskIds) {
        this.subtaskIds = subtaskIds;
    }

    public List<Long> getDependencyIds() {
        return dependencyIds;
    }

    public void setDependencyIds(List<Long> dependencyIds) {
        this.dependencyIds = dependencyIds;
    }
}
