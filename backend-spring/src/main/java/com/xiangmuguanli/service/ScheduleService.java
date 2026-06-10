package com.xiangmuguanli.service;

import com.xiangmuguanli.dto.response.EventResponse;
import com.xiangmuguanli.entity.Event;
import com.xiangmuguanli.entity.Project;
import com.xiangmuguanli.entity.Task;
import com.xiangmuguanli.entity.User;
import com.xiangmuguanli.exception.ResourceNotFoundException;
import com.xiangmuguanli.repository.EventRepository;
import com.xiangmuguanli.repository.ProjectRepository;
import com.xiangmuguanli.repository.TaskRepository;
import com.xiangmuguanli.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    private final EventRepository eventRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectService projectService;

    public ScheduleService(EventRepository eventRepository,
                           ProjectRepository projectRepository,
                           TaskRepository taskRepository,
                           UserRepository userRepository,
                           ProjectService projectService) {
        this.eventRepository = eventRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectService = projectService;
    }

    @Transactional
    public EventResponse createEvent(String title, String description, String location,
                                      Long userId, Long projectId, Long taskId,
                                      LocalDateTime startTime, LocalDateTime endTime,
                                      Boolean allDay, Integer reminderMinutes, String repeatRule) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Event event = new Event();
        event.setTitle(title);
        event.setDescription(description);
        event.setLocation(location);
        event.setUser(user);
        event.setStartTime(startTime);
        event.setEndTime(endTime);
        event.setAllDay(allDay != null ? allDay : false);
        event.setReminderMinutes(reminderMinutes != null ? reminderMinutes : 0);
        event.setRepeatRule(repeatRule);

        if (projectId != null) {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
            event.setProject(project);
        }

        if (taskId != null) {
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
            event.setTask(task);
        }

        event = eventRepository.save(event);
        return EventResponse.fromEntity(event);
    }

    public List<EventResponse> getEventsByUserAndDateRange(Long userId, LocalDateTime start, LocalDateTime end) {
        return eventRepository.findByUserIdAndStartTimeBetween(userId, start, end)
                .stream()
                .map(EventResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<EventResponse> getEventsByProject(Long projectId) {
        return eventRepository.findByProjectId(projectId)
                .stream()
                .map(EventResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public EventResponse getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));
        return EventResponse.fromEntity(event);
    }

    @Transactional
    public EventResponse updateEvent(Long id, String title, String description, String location,
                                      LocalDateTime startTime, LocalDateTime endTime,
                                      Boolean allDay, Integer reminderMinutes, String repeatRule) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));

        if (title != null && !title.isBlank()) {
            event.setTitle(title);
        }
        if (description != null) {
            event.setDescription(description);
        }
        if (location != null) {
            event.setLocation(location);
        }
        if (startTime != null) {
            event.setStartTime(startTime);
        }
        if (endTime != null) {
            event.setEndTime(endTime);
        }
        if (allDay != null) {
            event.setAllDay(allDay);
        }
        if (reminderMinutes != null) {
            event.setReminderMinutes(reminderMinutes);
        }
        if (repeatRule != null) {
            event.setRepeatRule(repeatRule);
        }

        event = eventRepository.save(event);
        return EventResponse.fromEntity(event);
    }

    @Transactional
    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));
        eventRepository.delete(event);
    }

    // SpEL-compatible permission helpers for @PreAuthorize

    public boolean isSelfOrAdmin(Long userId, String username) {
        User currentUser = userRepository.findByUsername(username).orElse(null);
        if (currentUser == null) return false;
        if (currentUser.isAdmin()) return true;
        return currentUser.getId().equals(userId);
    }

    public boolean checkEventAccess(Long eventId, String username) {
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) return false;
        User currentUser = userRepository.findByUsername(username).orElse(null);
        if (currentUser == null) return false;
        if (currentUser.isAdmin()) return true;
        if (event.getUser() != null && event.getUser().getId().equals(currentUser.getId())) return true;
        if (event.getProject() != null) {
            return projectService.checkProjectAccess(event.getProject().getId(), username);
        }
        return false;
    }
}
