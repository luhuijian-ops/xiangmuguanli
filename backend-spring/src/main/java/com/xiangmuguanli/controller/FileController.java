package com.xiangmuguanli.controller;

import com.xiangmuguanli.dto.response.ApiResponse;
import com.xiangmuguanli.dto.response.FileResponse;
import com.xiangmuguanli.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping
    @PreAuthorize("@projectService.checkProjectAccess(#projectId, authentication.name)")
    public ResponseEntity<ApiResponse<FileResponse>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false, defaultValue = "project") String entity,
            @RequestParam Long uploadedById) throws IOException {
        FileResponse fileResponse = fileService.uploadFile(file, projectId, taskId, entity, uploadedById);
        return ResponseEntity.ok(ApiResponse.success(fileResponse));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@fileService.checkFileAccess(#id, authentication.name)")
    public ResponseEntity<ApiResponse<FileResponse>> getFileById(@PathVariable Long id) {
        FileResponse file = fileService.getFileById(id);
        return ResponseEntity.ok(ApiResponse.success(file));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("@fileService.checkFileAccess(#id, authentication.name)")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) throws IOException {
        Resource resource = fileService.downloadFile(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("@projectService.checkProjectAccess(#projectId, authentication.name)")
    public ResponseEntity<ApiResponse<List<FileResponse>>> getFilesByProject(@PathVariable Long projectId) {
        List<FileResponse> files = fileService.getFilesByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(files));
    }

    @GetMapping("/task/{taskId}")
    @PreAuthorize("@taskService.checkTaskAccess(#taskId, authentication.name)")
    public ResponseEntity<ApiResponse<List<FileResponse>>> getFilesByTask(@PathVariable Long taskId) {
        List<FileResponse> files = fileService.getFilesByTask(taskId);
        return ResponseEntity.ok(ApiResponse.success(files));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@fileService.checkFileAdmin(#id, authentication.name)")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable Long id) throws IOException {
        fileService.deleteFile(id);
        return ResponseEntity.ok(ApiResponse.success("File deleted successfully", null));
    }
}
