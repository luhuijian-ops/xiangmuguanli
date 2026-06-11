package com.xiangmuguanli.controller;

import com.xiangmuguanli.dto.response.ApiResponse;
import com.xiangmuguanli.dto.response.EventResponse;
import com.xiangmuguanli.service.ScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping
    @PreAuthorize("@scheduleService.isSelfOrAdmin(#userId, authentication.name)")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String location,
            @RequestParam Long userId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long taskId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false, defaultValue = "false") Boolean allDay,
            @RequestParam(required = false, defaultValue = "0") Integer reminderMinutes,
            @RequestParam(required = false) String repeatRule) {
        EventResponse event = scheduleService.createEvent(title, description, location, userId,
                projectId, taskId, startTime, endTime, allDay, reminderMinutes, repeatRule);
        return ResponseEntity.ok(ApiResponse.success(event));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@scheduleService.checkEventAccess(#id, authentication.name)")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(@PathVariable Long id) {
        EventResponse event = scheduleService.getEventById(id);
        return ResponseEntity.ok(ApiResponse.success(event));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("@scheduleService.isSelfOrAdmin(#userId, authentication.name)")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getEventsByUserAndDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<EventResponse> events = scheduleService.getEventsByUserAndDateRange(userId, start, end);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("@projectService.checkProjectAccess(#projectId, authentication.name)")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getEventsByProject(@PathVariable Long projectId) {
        List<EventResponse> events = scheduleService.getEventsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@scheduleService.checkEventAccess(#id, authentication.name)")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable Long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) Boolean allDay,
            @RequestParam(required = false) Integer reminderMinutes,
            @RequestParam(required = false) String repeatRule) {
        EventResponse event = scheduleService.updateEvent(id, title, description, location,
                startTime, endTime, allDay, reminderMinutes, repeatRule);
        return ResponseEntity.ok(ApiResponse.success(event));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@scheduleService.checkEventAccess(#id, authentication.name)")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long id) {
        scheduleService.deleteEvent(id);
        return ResponseEntity.ok(ApiResponse.success("Event deleted successfully", null));
    }
}
