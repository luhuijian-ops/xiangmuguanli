package com.xiangmuguanli.dto.response;

import com.xiangmuguanli.entity.FileEntity;

import java.time.LocalDateTime;

public class FileResponse {

    private Long id;
    private String name;
    private String path;
    private Long size;
    private String type;
    private Long uploadedBy;
    private String uploaderName;
    private Long taskId;
    private Long projectId;
    private String entity;
    private LocalDateTime createdAt;

    public static FileResponse fromEntity(FileEntity file) {
        FileResponse response = new FileResponse();
        response.setId(file.getId());
        response.setName(file.getName());
        response.setPath(file.getPath());
        response.setSize(file.getSize());
        response.setType(file.getType());
        response.setUploadedBy(file.getUploader().getId());
        response.setUploaderName(file.getUploader().getName());
        response.setTaskId(file.getTask() != null ? file.getTask().getId() : null);
        response.setProjectId(file.getProject() != null ? file.getProject().getId() : null);
        response.setEntity(file.getEntity());
        response.setCreatedAt(file.getCreatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(Long uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
