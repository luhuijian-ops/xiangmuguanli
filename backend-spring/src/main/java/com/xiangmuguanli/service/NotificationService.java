package com.xiangmuguanli.service;

import com.xiangmuguanli.dto.response.NotificationResponse;
import com.xiangmuguanli.entity.Notification;
import com.xiangmuguanli.entity.User;
import com.xiangmuguanli.exception.ResourceNotFoundException;
import com.xiangmuguanli.repository.NotificationRepository;
import com.xiangmuguanli.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public NotificationResponse createNotification(Long userId, String title, String content,
                                                    String type, String entityType, Long entityId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setEntityType(entityType);
        notification.setEntityId(entityId);
        notification.setIsRead(false);

        return NotificationResponse.fromEntity(notificationRepository.save(notification));
    }

    public Page<NotificationResponse> getNotificationsByUser(Long userId, Boolean isRead, Pageable pageable) {
        if (isRead != null) {
            return notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, isRead, pageable)
                    .map(NotificationResponse::fromEntity);
        }
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationResponse::fromEntity);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    @Transactional
    public void markAsRead(Long id, Long userId) {
        notificationRepository.markAsRead(id, userId);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    // 快捷方法：任务分配通知
    @Transactional
    public void sendTaskAssignedNotification(Long userId, String taskTitle, Long taskId, String projectName) {
        createNotification(userId,
                "任务分配",
                "您被分配了新任务：" + taskTitle + (projectName != null ? "（项目：" + projectName + "）" : ""),
                "TASK_ASSIGNED",
                "TASK",
                taskId);
    }

    // 快捷方法：评论通知
    @Transactional
    public void sendCommentNotification(Long userId, String commenterName, String entityTitle,
                                         Long entityId, String entityType) {
        createNotification(userId,
                "新评论",
                commenterName + " 评论了 " + entityTitle,
                "COMMENT",
                entityType,
                entityId);
    }

    // 快捷方法：@提及通知
    @Transactional
    public void sendMentionNotification(Long userId, String mentionerName, String entityTitle,
                                         Long entityId, String entityType) {
        createNotification(userId,
                "您被提到了",
                mentionerName + " 在 " + entityTitle + " 中提到了您",
                "MENTION",
                entityType,
                entityId);
    }

    // 快捷方法：项目邀请通知
    @Transactional
    public void sendProjectInviteNotification(Long userId, String projectName, Long projectId, String inviterName) {
        createNotification(userId,
                "项目邀请",
                inviterName + " 邀请您加入项目：" + projectName,
                "PROJECT_INVITE",
                "PROJECT",
                projectId);
    }

    // 快捷方法：里程碑即将到期通知
    // SpEL-compatible permission helper for @PreAuthorize
    public boolean isNotificationOwner(Long notificationId, String username) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification == null) return false;
        User currentUser = userRepository.findByUsername(username).orElse(null);
        if (currentUser == null) return false;
        if (currentUser.isAdmin()) return true;
        return notification.getUser().getId().equals(currentUser.getId());
    }

    @Transactional
    public void sendMilestoneDueNotification(Long userId, String milestoneName, Long milestoneId, String projectName) {
        createNotification(userId,
                "里程碑即将到期",
                "里程碑 \"" + milestoneName + "\" 即将到期" + (projectName != null ? "（项目：" + projectName + "）" : ""),
                "MILESTONE_DUE",
                "MILESTONE",
                milestoneId);
    }
}
