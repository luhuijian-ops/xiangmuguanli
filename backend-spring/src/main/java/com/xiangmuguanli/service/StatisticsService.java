package com.xiangmuguanli.service;

import com.xiangmuguanli.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final WorkHourRepository workHourRepository;
    private final ActivityRepository activityRepository;

    public StatisticsService(ProjectRepository projectRepository,
                              TaskRepository taskRepository,
                              UserRepository userRepository,
                              WorkHourRepository workHourRepository,
                              ActivityRepository activityRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.workHourRepository = workHourRepository;
        this.activityRepository = activityRepository;
    }

    public Map<String, Object> getProjectStatistics(Long projectId) {
        return getProjectStatistics(projectId, null, null);
    }

    public Map<String, Object> getProjectStatistics(Long projectId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();

        List<com.xiangmuguanli.entity.Task> tasks = taskRepository.findByProjectId(projectId);
        if (startDate != null || endDate != null) {
            final LocalDate start = startDate != null ? startDate : LocalDate.MIN;
            final LocalDate end = endDate != null ? endDate : LocalDate.MAX;
            tasks = tasks.stream()
                    .filter(t -> t.getCreatedAt() != null)
                    .filter(t -> {
                        LocalDate createdAt = t.getCreatedAt().toLocalDate();
                        return !createdAt.isBefore(start) && !createdAt.isAfter(end);
                    })
                    .toList();
        }

        long totalTasks = tasks.size();
        long todoTasks = tasks.stream().filter(t -> t.getStatus() == com.xiangmuguanli.enums.TaskStatus.TODO).count();
        long doingTasks = tasks.stream().filter(t -> t.getStatus() == com.xiangmuguanli.enums.TaskStatus.DOING).count();
        long doneTasks = tasks.stream().filter(t -> t.getStatus() == com.xiangmuguanli.enums.TaskStatus.DONE).count();

        stats.put("totalTasks", totalTasks);
        stats.put("todoTasks", todoTasks);
        stats.put("doingTasks", doingTasks);
        stats.put("doneTasks", doneTasks);

        // 延期任务：截止时间已过期且状态不是 DONE/ARCHIVED
        java.time.LocalDate today = java.time.LocalDate.now();
        long overdueTasks = tasks.stream()
                .filter(t -> t.getDueDate() != null)
                .filter(t -> t.getDueDate().isBefore(today))
                .filter(t -> t.getStatus() != com.xiangmuguanli.enums.TaskStatus.DONE
                        && t.getStatus() != com.xiangmuguanli.enums.TaskStatus.ARCHIVED)
                .count();
        int overdueRate = totalTasks == 0 ? 0 : (int) Math.round((double) overdueTasks / totalTasks * 100);
        stats.put("overdueTasks", overdueTasks);
        stats.put("overdueRate", overdueRate);

        // 优先级分布
        Map<String, Long> priorityDistribution = new HashMap<>();
        for (int p = 0; p <= 4; p++) {
            final int priority = p;
            long count = tasks.stream().filter(t -> t.getPriority() != null && t.getPriority() == priority).count();
            priorityDistribution.put("P" + priority, count);
        }
        stats.put("priorityDistribution", priorityDistribution);

        // 成员贡献（按负责人分组统计任务数）
        Map<String, Long> memberContributions = new HashMap<>();
        tasks.stream()
                .filter(t -> t.getAssignedUser() != null)
                .forEach(t -> {
                    String name = t.getAssignedUser().getName();
                    memberContributions.merge(name, 1L, Long::sum);
                });
        stats.put("memberContributions", memberContributions);

        // 近 7 天任务创建趋势
        java.util.List<String> dates = new java.util.ArrayList<>();
        java.util.List<Long> createdTrend = new java.util.ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            java.time.LocalDate date = today.minusDays(i);
            dates.add(date.toString());
            final java.time.LocalDate dayStart = date;
            final java.time.LocalDate dayEnd = date.plusDays(1);
            long createdCount = tasks.stream()
                    .filter(t -> t.getCreatedAt() != null)
                    .filter(t -> {
                        java.time.LocalDate d = t.getCreatedAt().toLocalDate();
                        return !d.isBefore(dayStart) && d.isBefore(dayEnd);
                    })
                    .count();
            createdTrend.add(createdCount);
        }
        Map<String, Object> weeklyTrend = new HashMap<>();
        weeklyTrend.put("dates", dates);
        weeklyTrend.put("created", createdTrend);
        stats.put("weeklyTrend", weeklyTrend);

        // 活动时间轴（最近 20 条）
        List<Map<String, Object>> recentActivity = activityRepository
                .findByProjectIdOrderByCreatedAtDesc(projectId, org.springframework.data.domain.PageRequest.of(0, 20))
                .getContent()
                .stream()
                .map(a -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", a.getId());
                    item.put("action", a.getAction());
                    item.put("entityType", a.getEntityType());
                    item.put("entityId", a.getEntityId());
                    item.put("userName", a.getUser() != null ? a.getUser().getName() : null);
                    item.put("createdAt", a.getCreatedAt() != null ? a.getCreatedAt().toString() : null);
                    return item;
                })
                .toList();
        stats.put("recentActivity", recentActivity);

        return stats;
    }

    public Map<String, Object> getUserStatistics(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        List<com.xiangmuguanli.entity.Task> assignedTaskList = taskRepository.findByAssignedUserId(userId);
        long assignedTasks = assignedTaskList.size();
        long createdTasks = taskRepository.findByCreatedByUserId(userId).size();
        BigDecimal totalWorkHours = workHourRepository.findByUserId(userId).stream()
                .map(w -> w.getHours())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.put("assignedTasks", assignedTasks);
        stats.put("createdTasks", createdTasks);
        stats.put("totalWorkHours", totalWorkHours);

        // 任务状态分布
        long userTodo = assignedTaskList.stream().filter(t -> t.getStatus() == com.xiangmuguanli.enums.TaskStatus.TODO).count();
        long userDoing = assignedTaskList.stream().filter(t -> t.getStatus() == com.xiangmuguanli.enums.TaskStatus.DOING).count();
        long userDone = assignedTaskList.stream().filter(t -> t.getStatus() == com.xiangmuguanli.enums.TaskStatus.DONE).count();

        Map<String, Long> statusBreakdown = new HashMap<>();
        statusBreakdown.put("TODO", userTodo);
        statusBreakdown.put("DOING", userDoing);
        statusBreakdown.put("DONE", userDone);
        stats.put("statusBreakdown", statusBreakdown);

        // 完成率
        long userTotal = userTodo + userDoing + userDone;
        int completionRate = userTotal == 0 ? 0 : (int) Math.round((double) userDone / userTotal * 100);
        stats.put("completionRate", completionRate);

        return stats;
    }

    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalProjects = projectRepository.count();
        long totalUsers = userRepository.count();
        long totalTasks = taskRepository.count();

        stats.put("totalProjects", totalProjects);
        stats.put("totalUsers", totalUsers);
        stats.put("totalTasks", totalTasks);

        // 近7天任务创建与完成趋势
        java.util.List<String> dates = new java.util.ArrayList<>();
        java.util.List<Long> createdTrend = new java.util.ArrayList<>();
        java.util.List<Long> completedTrend = new java.util.ArrayList<>();

        java.time.LocalDate today = java.time.LocalDate.now();
        java.util.List<com.xiangmuguanli.entity.Task> allTasks = taskRepository.findAll();

        for (int i = 6; i >= 0; i--) {
            java.time.LocalDate date = today.minusDays(i);
            dates.add(date.toString());

            final java.time.LocalDate dayStart = date;
            final java.time.LocalDate dayEnd = date.plusDays(1);

            long createdCount = allTasks.stream()
                    .filter(t -> t.getCreatedAt() != null)
                    .filter(t -> {
                        java.time.LocalDate d = t.getCreatedAt().toLocalDate();
                        return !d.isBefore(dayStart) && d.isBefore(dayEnd);
                    })
                    .count();

            long completedCount = allTasks.stream()
                    .filter(t -> t.getStatus() == com.xiangmuguanli.enums.TaskStatus.DONE)
                    .filter(t -> t.getUpdatedAt() != null)
                    .filter(t -> {
                        java.time.LocalDate d = t.getUpdatedAt().toLocalDate();
                        return !d.isBefore(dayStart) && d.isBefore(dayEnd);
                    })
                    .count();

            createdTrend.add(createdCount);
            completedTrend.add(completedCount);
        }

        Map<String, Object> weeklyTrend = new HashMap<>();
        weeklyTrend.put("dates", dates);
        weeklyTrend.put("created", createdTrend);
        weeklyTrend.put("completed", completedTrend);
        stats.put("weeklyTrend", weeklyTrend);

        return stats;
    }

    public Map<String, Object> getWorkHourStatistics(Long userId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();

        var workHours = workHourRepository.findByUserIdAndDateBetween(userId, startDate, endDate);

        BigDecimal totalHours = workHours.stream()
                .map(w -> w.getHours())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.put("totalHours", totalHours);
        stats.put("workHourCount", workHours.size());

        return stats;
    }
}
