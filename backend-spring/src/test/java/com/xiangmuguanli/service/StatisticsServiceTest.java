package com.xiangmuguanli.service;

import com.xiangmuguanli.entity.*;
import com.xiangmuguanli.enums.TaskStatus;
import com.xiangmuguanli.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkHourRepository workHourRepository;

    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    private User user;
    private Task taskTodo;
    private Task taskDone;
    private Task taskOverdue;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Alice");

        Project project = new Project();
        project.setId(1L);

        taskTodo = createTask(1L, "Todo Task", TaskStatus.TODO, 2, null, project, user);
        taskDone = createTask(2L, "Done Task", TaskStatus.DONE, 3, LocalDate.now().minusDays(1), project, user);
        taskOverdue = createTask(3L, "Overdue Task", TaskStatus.DOING, 1, LocalDate.now().minusDays(2), project, user);
    }

    private Task createTask(Long id, String title, TaskStatus status, int priority,
                            LocalDate dueDate, Project project, User assignee) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setStatus(status);
        task.setPriority(priority);
        task.setDueDate(dueDate);
        task.setProject(project);
        task.setAssignedUser(assignee);
        task.setCreatedByUser(assignee);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return task;
    }

    @Test
    void getProjectStatistics_returnsTaskCountsAndOverdueRate() {
        when(taskRepository.findByProjectId(1L)).thenReturn(List.of(taskTodo, taskDone, taskOverdue));
        when(activityRepository.findByProjectIdOrderByCreatedAtDesc(eq(1L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));

        Map<String, Object> stats = statisticsService.getProjectStatistics(1L);

        assertThat(stats).containsEntry("totalTasks", 3L);
        assertThat(stats).containsEntry("todoTasks", 1L);
        assertThat(stats).containsEntry("doingTasks", 1L);
        assertThat(stats).containsEntry("doneTasks", 1L);
        assertThat(stats).containsEntry("overdueTasks", 1L);
        assertThat(stats).containsEntry("overdueRate", 33);
        assertThat(stats).containsKey("priorityDistribution");
        assertThat(stats).containsKey("memberContributions");
        assertThat(stats).containsKey("recentActivity");
    }

    @Test
    void getProjectStatistics_withDateRange_filtersTasksByCreatedAt() {
        taskTodo.setCreatedAt(LocalDateTime.now().minusDays(10));
        taskDone.setCreatedAt(LocalDateTime.now());
        taskOverdue.setCreatedAt(LocalDateTime.now());

        when(taskRepository.findByProjectId(1L)).thenReturn(List.of(taskTodo, taskDone, taskOverdue));
        when(activityRepository.findByProjectIdOrderByCreatedAtDesc(eq(1L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));

        Map<String, Object> stats = statisticsService.getProjectStatistics(1L, LocalDate.now().minusDays(1), LocalDate.now());

        assertThat(stats).containsEntry("totalTasks", 2L);
    }

    @Test
    void getProjectStatistics_withNoTasks_returnsZeroRates() {
        when(taskRepository.findByProjectId(1L)).thenReturn(List.of());
        when(activityRepository.findByProjectIdOrderByCreatedAtDesc(eq(1L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));

        Map<String, Object> stats = statisticsService.getProjectStatistics(1L);

        assertThat(stats).containsEntry("totalTasks", 0L);
        assertThat(stats).containsEntry("overdueRate", 0);
    }

    @Test
    void getUserStatistics_returnsAssignedCreatedAndWorkHours() {
        when(taskRepository.findByAssignedUserId(1L)).thenReturn(List.of(taskTodo, taskDone));
        when(taskRepository.findByCreatedByUserId(1L)).thenReturn(List.of(taskOverdue));
        WorkHour wh = new WorkHour();
        wh.setHours(new BigDecimal("4.5"));
        when(workHourRepository.findByUserId(1L)).thenReturn(List.of(wh));

        Map<String, Object> stats = statisticsService.getUserStatistics(1L);

        assertThat(stats).containsEntry("assignedTasks", 2L);
        assertThat(stats).containsEntry("createdTasks", 1L);
        assertThat(stats).containsEntry("totalWorkHours", new BigDecimal("4.5"));
        assertThat(stats).containsEntry("completionRate", 50);
    }

    @Test
    void getDashboardStatistics_returnsTotalsAndWeeklyTrend() {
        when(projectRepository.count()).thenReturn(5L);
        when(userRepository.count()).thenReturn(10L);
        when(taskRepository.count()).thenReturn(20L);
        when(taskRepository.findAll()).thenReturn(List.of(taskTodo, taskDone, taskOverdue));

        Map<String, Object> stats = statisticsService.getDashboardStatistics();

        assertThat(stats).containsEntry("totalProjects", 5L);
        assertThat(stats).containsEntry("totalUsers", 10L);
        assertThat(stats).containsEntry("totalTasks", 20L);
        assertThat(stats).containsKey("weeklyTrend");
    }
}
