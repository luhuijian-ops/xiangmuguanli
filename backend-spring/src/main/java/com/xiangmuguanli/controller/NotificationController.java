package com.xiangmuguanli.controller;

import com.xiangmuguanli.dto.response.ApiResponse;
import com.xiangmuguanli.dto.response.NotificationResponse;
import com.xiangmuguanli.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("@userService.isSelfOrAdmin(#userId, authentication.name)")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotificationsByUser(
            @PathVariable Long userId,
            @RequestParam(required = false) Boolean isRead,
            Pageable pageable) {
        Page<NotificationResponse> notifications = notificationService.getNotificationsByUser(userId, isRead, pageable);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/user/{userId}/unread-count")
    @PreAuthorize("@userService.isSelfOrAdmin(#userId, authentication.name)")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@PathVariable Long userId) {
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("@notificationService.isNotificationOwner(#id, authentication.name)")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id, @RequestParam Long userId) {
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Marked as read", null));
    }

    @PutMapping("/user/{userId}/read-all")
    @PreAuthorize("@userService.isSelfOrAdmin(#userId, authentication.name)")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }
}
