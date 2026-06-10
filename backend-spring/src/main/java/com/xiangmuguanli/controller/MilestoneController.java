package com.xiangmuguanli.controller;

import com.xiangmuguanli.dto.response.ApiResponse;
import com.xiangmuguanli.dto.response.MilestoneResponse;
import com.xiangmuguanli.enums.MilestoneStatus;
import com.xiangmuguanli.service.MilestoneService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/milestones")
public class MilestoneController {

    private final MilestoneService milestoneService;

    public MilestoneController(MilestoneService milestoneService) {
        this.milestoneService = milestoneService;
    }

    @PostMapping
    @PreAuthorize("@projectService.checkProjectAdmin(#projectId, authentication.name)")
    public ResponseEntity<ApiResponse<MilestoneResponse>> createMilestone(
            @RequestParam Long projectId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            @RequestParam(required = false) Integer orderIndex) {
        MilestoneResponse milestone = milestoneService.createMilestone(projectId, name, description, dueDate, orderIndex);
        return ResponseEntity.ok(ApiResponse.success(milestone));
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("@projectService.checkProjectAccess(#projectId, authentication.name)")
    public ResponseEntity<ApiResponse<List<MilestoneResponse>>> getMilestonesByProject(@PathVariable Long projectId) {
        List<MilestoneResponse> milestones = milestoneService.getMilestonesByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(milestones));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@milestoneService.checkMilestoneAccess(#id, authentication.name)")
    public ResponseEntity<ApiResponse<MilestoneResponse>> getMilestoneById(@PathVariable Long id) {
        MilestoneResponse milestone = milestoneService.getMilestoneById(id);
        return ResponseEntity.ok(ApiResponse.success(milestone));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@milestoneService.checkMilestoneAdmin(#id, authentication.name)")
    public ResponseEntity<ApiResponse<MilestoneResponse>> updateMilestone(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            @RequestParam(required = false) MilestoneStatus status,
            @RequestParam(required = false) Integer orderIndex) {
        MilestoneResponse milestone = milestoneService.updateMilestone(id, name, description, dueDate, status, orderIndex);
        return ResponseEntity.ok(ApiResponse.success(milestone));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@milestoneService.checkMilestoneAdmin(#id, authentication.name)")
    public ResponseEntity<ApiResponse<Void>> deleteMilestone(@PathVariable Long id) {
        milestoneService.deleteMilestone(id);
        return ResponseEntity.ok(ApiResponse.success("Milestone deleted successfully", null));
    }
}
