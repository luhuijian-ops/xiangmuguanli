package com.xiangmuguanli.service;

import com.xiangmuguanli.dto.response.MilestoneResponse;
import com.xiangmuguanli.entity.Milestone;
import com.xiangmuguanli.entity.Project;
import com.xiangmuguanli.enums.MilestoneStatus;
import com.xiangmuguanli.exception.ResourceNotFoundException;
import com.xiangmuguanli.repository.MilestoneRepository;
import com.xiangmuguanli.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final ProjectRepository projectRepository;
    private final ProjectService projectService;

    public MilestoneService(MilestoneRepository milestoneRepository, ProjectRepository projectRepository, ProjectService projectService) {
        this.milestoneRepository = milestoneRepository;
        this.projectRepository = projectRepository;
        this.projectService = projectService;
    }

    @Transactional
    public MilestoneResponse createMilestone(Long projectId, String name, String description,
                                               LocalDate dueDate, Integer orderIndex) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        Milestone milestone = new Milestone();
        milestone.setProject(project);
        milestone.setName(name);
        milestone.setDescription(description);
        milestone.setDueDate(dueDate);
        milestone.setOrderIndex(orderIndex != null ? orderIndex : 0);
        milestone.setStatus(MilestoneStatus.PENDING);

        milestone = milestoneRepository.save(milestone);
        return MilestoneResponse.fromEntity(milestone);
    }

    public List<MilestoneResponse> getMilestonesByProject(Long projectId) {
        return milestoneRepository.findByProjectIdOrderByOrderIndexAsc(projectId)
                .stream()
                .map(MilestoneResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public MilestoneResponse getMilestoneById(Long id) {
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone", "id", id));
        return MilestoneResponse.fromEntity(milestone);
    }

    @Transactional
    public MilestoneResponse updateMilestone(Long id, String name, String description,
                                              LocalDate dueDate, MilestoneStatus status, Integer orderIndex) {
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone", "id", id));

        if (name != null && !name.isBlank()) {
            milestone.setName(name);
        }
        if (description != null) {
            milestone.setDescription(description);
        }
        if (dueDate != null) {
            milestone.setDueDate(dueDate);
        }
        if (status != null) {
            milestone.setStatus(status);
        }
        if (orderIndex != null) {
            milestone.setOrderIndex(orderIndex);
        }

        milestone = milestoneRepository.save(milestone);
        return MilestoneResponse.fromEntity(milestone);
    }

    @Transactional
    public void deleteMilestone(Long id) {
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone", "id", id));
        milestoneRepository.delete(milestone);
    }

    // SpEL-compatible permission helpers for @PreAuthorize

    public boolean checkMilestoneAccess(Long milestoneId, String username) {
        Milestone milestone = milestoneRepository.findById(milestoneId).orElse(null);
        if (milestone == null) return false;
        return projectService.checkProjectAccess(milestone.getProject().getId(), username);
    }

    public boolean checkMilestoneAdmin(Long milestoneId, String username) {
        Milestone milestone = milestoneRepository.findById(milestoneId).orElse(null);
        if (milestone == null) return false;
        return projectService.checkProjectAdmin(milestone.getProject().getId(), username);
    }
}
