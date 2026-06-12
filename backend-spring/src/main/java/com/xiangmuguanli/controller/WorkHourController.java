package com.xiangmuguanli.controller;

import com.xiangmuguanli.dto.response.ApiResponse;
import com.xiangmuguanli.dto.response.WorkHourResponse;
import com.xiangmuguanli.service.WorkHourService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/work-hours")
public class WorkHourController {

    private final WorkHourService workHourService;

    public WorkHourController(WorkHourService workHourService) {
        this.workHourService = workHourService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WorkHourResponse>> createWorkHour(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long taskId,
            @RequestParam BigDecimal hours,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String description) {
        WorkHourResponse workHour = workHourService.createWorkHour(userDetails.getUsername(), projectId, taskId, hours, date, description);
        return ResponseEntity.ok(ApiResponse.success(workHour));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("@workHourService.isSelfOrAdmin(#userId, authentication.name)")
    public ResponseEntity<ApiResponse<List<WorkHourResponse>>> getWorkHoursByUserAndDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<WorkHourResponse> workHours = workHourService.getWorkHoursByUserAndDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(workHours));
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("@projectService.checkProjectAccess(#projectId, authentication.name)")
    public ResponseEntity<ApiResponse<List<WorkHourResponse>>> getWorkHoursByProject(@PathVariable Long projectId) {
        List<WorkHourResponse> workHours = workHourService.getWorkHoursByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(workHours));
    }

    @GetMapping("/task/{taskId}")
    @PreAuthorize("@taskService.checkTaskAccess(#taskId, authentication.name)")
    public ResponseEntity<ApiResponse<List<WorkHourResponse>>> getWorkHoursByTask(@PathVariable Long taskId) {
        List<WorkHourResponse> workHours = workHourService.getWorkHoursByTask(taskId);
        return ResponseEntity.ok(ApiResponse.success(workHours));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@workHourService.isWorkHourOwnerOrAdmin(#id, authentication.name)")
    public ResponseEntity<ApiResponse<WorkHourResponse>> updateWorkHour(
            @PathVariable Long id,
            @RequestParam(required = false) BigDecimal hours,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String description) {
        WorkHourResponse workHour = workHourService.updateWorkHour(id, hours, date, description);
        return ResponseEntity.ok(ApiResponse.success(workHour));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@workHourService.isWorkHourOwnerOrAdmin(#id, authentication.name)")
    public ResponseEntity<ApiResponse<Void>> deleteWorkHour(@PathVariable Long id) {
        workHourService.deleteWorkHour(id);
        return ResponseEntity.ok(ApiResponse.success("Work hour deleted successfully", null));
    }
}
