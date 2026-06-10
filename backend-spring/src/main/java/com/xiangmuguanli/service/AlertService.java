package com.xiangmuguanli.service;

import com.xiangmuguanli.entity.Alert;
import com.xiangmuguanli.entity.User;
import com.xiangmuguanli.enums.AlertType;
import com.xiangmuguanli.enums.Severity;
import com.xiangmuguanli.repository.AlertRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertService {

    private final AlertRepository alertRepository;

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Transactional
    public Alert createAlert(AlertType type, Severity severity, User user, Long targetId, String targetType, String message) {
        Alert alert = new Alert();
        alert.setType(type);
        alert.setSeverity(severity);
        alert.setUser(user);
        alert.setTargetId(targetId);
        alert.setTargetType(targetType);
        alert.setMessage(message);
        alert.setResolved(false);
        return alertRepository.save(alert);
    }

    public Page<Alert> getAllAlerts(Pageable pageable) {
        return alertRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public Page<Alert> getAlertsByType(AlertType type, Pageable pageable) {
        return alertRepository.findByTypeOrderByCreatedAtDesc(type, pageable);
    }

    public Page<Alert> getUnresolvedAlerts(Pageable pageable) {
        return alertRepository.findByResolvedOrderByCreatedAtDesc(false, pageable);
    }

    @Transactional
    public Alert resolveAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new com.xiangmuguanli.exception.ResourceNotFoundException("Alert", "id", alertId));
        alert.setResolved(true);
        return alertRepository.save(alert);
    }
}
