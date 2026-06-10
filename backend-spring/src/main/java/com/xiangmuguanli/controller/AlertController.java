package com.xiangmuguanli.controller;

import com.xiangmuguanli.dto.response.AlertResponse;
import com.xiangmuguanli.dto.response.ApiResponse;
import com.xiangmuguanli.entity.Alert;
import com.xiangmuguanli.enums.AlertType;
import com.xiangmuguanli.service.AlertService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AlertResponse>>> getAllAlerts(Pageable pageable) {
        Page<AlertResponse> alerts = alertService.getAllAlerts(pageable).map(AlertResponse::fromEntity);
        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AlertResponse>>> getAlertsByType(
            @PathVariable AlertType type, Pageable pageable) {
        Page<AlertResponse> alerts = alertService.getAlertsByType(type, pageable).map(AlertResponse::fromEntity);
        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    @GetMapping("/unresolved")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AlertResponse>>> getUnresolvedAlerts(Pageable pageable) {
        Page<AlertResponse> alerts = alertService.getUnresolvedAlerts(pageable).map(AlertResponse::fromEntity);
        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AlertResponse>> resolveAlert(@PathVariable Long id) {
        Alert alert = alertService.resolveAlert(id);
        return ResponseEntity.ok(ApiResponse.success(AlertResponse.fromEntity(alert)));
    }
}
