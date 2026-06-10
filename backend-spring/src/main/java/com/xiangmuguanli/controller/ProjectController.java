package com.xiangmuguanli.controller;

import com.xiangmuguanli.dto.request.ProjectCreateRequest;
import com.xiangmuguanli.dto.request.ProjectUpdateRequest;
import com.xiangmuguanli.dto.response.ApiResponse;
import com.xiangmuguanli.dto.response.ProjectResponse;
import com.xiangmuguanli.entity.ProjectMember;
import com.xiangmuguanli.enums.MemberRole;
import com.xiangmuguanli.service.ProjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> getAllProjects(Pageable pageable) {
        Page<ProjectResponse> projects = projectService.getAllProjects(pageable);
        return ResponseEntity.ok(ApiResponse.success(projects));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> getMyProjects(
            @AuthenticationPrincipal UserDetails userDetails, Pageable pageable) {
        Page<ProjectResponse> projects = projectService.getMyProjects(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(ApiResponse.success(projects));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@projectService.checkProjectAccess(#id, authentication.name)")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(@PathVariable Long id) {
        ProjectResponse project = projectService.getProjectById(id);
        return ResponseEntity.ok(ApiResponse.success(project));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("@projectService.checkProjectAccessByCode(#code, authentication.name)")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectByCode(@PathVariable String code) {
        ProjectResponse project = projectService.getProjectByCode(code);
        return ResponseEntity.ok(ApiResponse.success(project));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> getProjectsByOwner(
            @PathVariable Long ownerId, Pageable pageable) {
        Page<ProjectResponse> projects = projectService.getProjectsByOwner(ownerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(projects));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody ProjectCreateRequest request) {
        ProjectResponse project = projectService.createProject(
                request.getName(), request.getDescription(), request.getCode(),
                request.getOwnerId(), request.getStartDate(), request.getEndDate(),
                request.getBudget(), request.getRemarks(), request.getPriority());
        return ResponseEntity.ok(ApiResponse.success(project));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@projectService.checkProjectAdmin(#id, authentication.name)")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable Long id,
            @RequestBody ProjectUpdateRequest request) {
        ProjectResponse project = projectService.updateProject(
                id, request.getName(), request.getDescription(),
                request.getStartDate(), request.getEndDate(), request.getStatus(),
                request.getBudget(), request.getRemarks(), request.getPriority());
        return ResponseEntity.ok(ApiResponse.success(project));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@projectService.checkProjectOwner(#id, authentication.name)")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.success("Project deleted successfully", null));
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("@projectService.checkProjectOwner(#id, authentication.name)")
    public ResponseEntity<ApiResponse<ProjectResponse>> archiveProject(@PathVariable Long id) {
        ProjectResponse project = projectService.archiveProject(id);
        return ResponseEntity.ok(ApiResponse.success(project));
    }

    @PostMapping("/{id}/unarchive")
    @PreAuthorize("@projectService.checkProjectOwner(#id, authentication.name)")
    public ResponseEntity<ApiResponse<ProjectResponse>> unarchiveProject(@PathVariable Long id) {
        ProjectResponse project = projectService.unarchiveProject(id);
        return ResponseEntity.ok(ApiResponse.success(project));
    }

    @GetMapping("/{id}/members")
    @PreAuthorize("@projectService.checkProjectAccess(#id, authentication.name)")
    public ResponseEntity<ApiResponse<List<ProjectMember>>> getProjectMembers(@PathVariable Long id) {
        List<ProjectMember> members = projectService.getProjectMembers(id);
        return ResponseEntity.ok(ApiResponse.success(members));
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("@projectService.checkProjectAdmin(#id, authentication.name)")
    public ResponseEntity<ApiResponse<ProjectMember>> addMember(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam(required = false) MemberRole role) {
        ProjectMember member = projectService.addMember(id, userId, role);
        return ResponseEntity.ok(ApiResponse.success(member));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("@projectService.checkProjectAdmin(#id, authentication.name)")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long id, @PathVariable Long userId) {
        projectService.removeMember(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully", null));
    }

    @PutMapping("/{id}/members/{userId}")
    @PreAuthorize("@projectService.checkProjectOwner(#id, authentication.name)")
    public ResponseEntity<ApiResponse<ProjectMember>> updateMemberRole(
            @PathVariable Long id,
            @PathVariable Long userId,
            @RequestParam MemberRole role) {
        ProjectMember member = projectService.updateMemberRole(id, userId, role);
        return ResponseEntity.ok(ApiResponse.success(member));
    }
}
