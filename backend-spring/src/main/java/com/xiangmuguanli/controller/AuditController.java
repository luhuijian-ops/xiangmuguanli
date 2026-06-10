package com.xiangmuguanli.controller;

import com.xiangmuguanli.dto.response.ActivityResponse;
import com.xiangmuguanli.dto.response.ApiResponse;
import com.xiangmuguanli.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("@projectService.checkProjectAccess(#projectId, authentication.name)")
    public ResponseEntity<ApiResponse<Page<ActivityResponse>>> getActivitiesByProject(
            @PathVariable Long projectId, Pageable pageable) {
        Page<ActivityResponse> activities = auditService.getActivitiesByProject(projectId, pageable);
        return ResponseEntity.ok(ApiResponse.success(activities));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("@userService.isSelfOrAdmin(#userId, authentication.name)")
    public ResponseEntity<ApiResponse<Page<ActivityResponse>>> getActivitiesByUser(
            @PathVariable Long userId, Pageable pageable) {
        Page<ActivityResponse> activities = auditService.getActivitiesByUser(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(activities));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<ApiResponse<Page<ActivityResponse>>> getActivitiesByEntity(
            @PathVariable String entityType, @PathVariable Long entityId, Pageable pageable) {
        Page<ActivityResponse> activities = auditService.getActivitiesByEntity(entityType, entityId, pageable);
        return ResponseEntity.ok(ApiResponse.success(activities));
    }

    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<ActivityResponse>>> getAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Pageable pageable) {
        Page<ActivityResponse> activities = auditService.getAuditLogs(action, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(activities));
    }
}
