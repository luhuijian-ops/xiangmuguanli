package com.xiangmuguanli.controller;

import com.xiangmuguanli.dto.response.ApiResponse;
import com.xiangmuguanli.service.StatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("@projectService.checkProjectAccess(#projectId, authentication.name)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProjectStatistics(
            @PathVariable Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> stats = statisticsService.getProjectStatistics(projectId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("@userService.isSelfOrAdmin(#userId, authentication.name)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStatistics(@PathVariable Long userId) {
        Map<String, Object> stats = statisticsService.getUserStatistics(userId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStatistics() {
        Map<String, Object> stats = statisticsService.getDashboardStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/work-hours")
    @PreAuthorize("@userService.isSelfOrAdmin(#userId, authentication.name)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWorkHourStatistics(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> stats = statisticsService.getWorkHourStatistics(userId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
