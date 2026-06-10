package com.xiangmuguanli.service;

import com.xiangmuguanli.config.FileConfig;
import com.xiangmuguanli.dto.response.FileResponse;
import com.xiangmuguanli.entity.FileEntity;
import com.xiangmuguanli.entity.Project;
import com.xiangmuguanli.entity.Task;
import com.xiangmuguanli.entity.User;
import com.xiangmuguanli.exception.ResourceNotFoundException;
import com.xiangmuguanli.repository.FileRepository;
import com.xiangmuguanli.repository.ProjectRepository;
import com.xiangmuguanli.repository.TaskRepository;
import com.xiangmuguanli.repository.UserRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileService {

    private final FileRepository fileRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final FileConfig fileConfig;
    private final ProjectService projectService;

    public FileService(FileRepository fileRepository,
                       ProjectRepository projectRepository,
                       TaskRepository taskRepository,
                       UserRepository userRepository,
                       FileConfig fileConfig,
                       ProjectService projectService) {
        this.fileRepository = fileRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.fileConfig = fileConfig;
        this.projectService = projectService;
    }

    @Transactional
    public FileResponse uploadFile(MultipartFile file, Long projectId, Long taskId,
                                   String entity, Long uploadedById) throws IOException {
        User uploader = userRepository.findById(uploadedById)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", uploadedById));

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String newFilename = UUID.randomUUID().toString() + extension;

        Path targetDir = Paths.get(fileConfig.getUploadDir()).toAbsolutePath().normalize();
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }
        Path targetPath = targetDir.resolve(newFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        FileEntity fileEntity = new FileEntity();
        fileEntity.setName(originalFilename);
        fileEntity.setPath(targetPath.toString());
        fileEntity.setSize(file.getSize());
        fileEntity.setType(file.getContentType());
        fileEntity.setUploader(uploader);
        fileEntity.setEntity(entity != null ? entity : "project");

        if (projectId != null) {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
            fileEntity.setProject(project);
        }

        if (taskId != null) {
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
            fileEntity.setTask(task);
        }

        fileEntity = fileRepository.save(fileEntity);
        return FileResponse.fromEntity(fileEntity);
    }

    public Resource downloadFile(Long fileId) throws MalformedURLException {
        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", fileId));

        Path filePath = Paths.get(fileEntity.getPath()).toAbsolutePath().normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new ResourceNotFoundException("File not found or not readable");
        }
    }

    public FileResponse getFileById(Long id) {
        FileEntity file = fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", id));
        return FileResponse.fromEntity(file);
    }

    public List<FileResponse> getFilesByProject(Long projectId) {
        return fileRepository.findByProjectId(projectId).stream()
                .map(FileResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<FileResponse> getFilesByTask(Long taskId) {
        return fileRepository.findByTaskId(taskId).stream()
                .map(FileResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteFile(Long fileId) throws IOException {
        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", fileId));

        Path filePath = Paths.get(fileEntity.getPath());
        Files.deleteIfExists(filePath);

        fileRepository.delete(fileEntity);
    }

    // SpEL-compatible permission helpers for @PreAuthorize

    public boolean checkFileAccess(Long fileId, String username) {
        FileEntity file = fileRepository.findById(fileId).orElse(null);
        if (file == null) return false;
        if (file.getProject() != null) {
            return projectService.checkProjectAccess(file.getProject().getId(), username);
        }
        if (file.getTask() != null) {
            return projectService.checkProjectAccess(file.getTask().getProject().getId(), username);
        }
        return true;
    }

    public boolean checkFileAdmin(Long fileId, String username) {
        FileEntity file = fileRepository.findById(fileId).orElse(null);
        if (file == null) return false;
        if (file.getProject() != null) {
            return projectService.checkProjectAdmin(file.getProject().getId(), username);
        }
        if (file.getTask() != null) {
            return projectService.checkProjectAdmin(file.getTask().getProject().getId(), username);
        }
        return true;
    }
}
