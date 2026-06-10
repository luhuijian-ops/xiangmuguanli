package com.xiangmuguanli.service;

import com.xiangmuguanli.dto.response.ProjectResponse;
import com.xiangmuguanli.entity.Project;
import com.xiangmuguanli.entity.ProjectMember;
import com.xiangmuguanli.entity.User;
import com.xiangmuguanli.enums.MemberRole;
import com.xiangmuguanli.enums.ProjectPriority;
import com.xiangmuguanli.enums.ProjectStatus;
import com.xiangmuguanli.exception.BadRequestException;
import com.xiangmuguanli.exception.ForbiddenException;
import com.xiangmuguanli.exception.ResourceNotFoundException;
import com.xiangmuguanli.repository.ProjectMemberRepository;
import com.xiangmuguanli.repository.ProjectRepository;
import com.xiangmuguanli.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectMemberRepository projectMemberRepository,
                          UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> getAllProjects(Pageable pageable) {
        return projectRepository.findAll(pageable).map(ProjectResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        return ProjectResponse.fromEntity(project);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectByCode(String code) {
        Project project = projectRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "code", code));
        return ProjectResponse.fromEntity(project);
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> getProjectsByOwner(Long ownerId, Pageable pageable) {
        return projectRepository.findByOwnerId(ownerId, pageable).map(ProjectResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> getMyProjects(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        List<ProjectMember> memberships = projectMemberRepository.findByUserId(user.getId());
        List<ProjectResponse> projects = memberships.stream()
                .map(ProjectMember::getProject)
                .distinct()
                .map(ProjectResponse::fromEntity)
                .toList();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), projects.size());
        List<ProjectResponse> pageContent = start > projects.size() ? List.of() : projects.subList(start, end);
        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, projects.size());
    }

    @Transactional
    public ProjectResponse createProject(String name, String description, String code,
                                          Long ownerId, LocalDate startDate, LocalDate endDate,
                                          BigDecimal budget, String remarks, ProjectPriority priority) {
        if (projectRepository.existsByCode(code)) {
            throw new BadRequestException("Project code already exists");
        }

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", ownerId));

        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setCode(code);
        project.setOwner(owner);
        project.setStartDate(startDate);
        project.setEndDate(endDate);
        project.setBudget(budget);
        project.setRemarks(remarks);
        project.setStatus(ProjectStatus.ACTIVE);
        if (priority != null) {
            project.setPriority(priority);
        }

        project = projectRepository.save(project);

        // Add owner as project member with OWNER role
        ProjectMember ownerMember = new ProjectMember();
        ownerMember.setProject(project);
        ownerMember.setUser(owner);
        ownerMember.setRole(MemberRole.OWNER);
        projectMemberRepository.save(ownerMember);

        return ProjectResponse.fromEntity(project);
    }

    private void validateProjectNotArchived(Long projectId) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project != null && project.getStatus() == ProjectStatus.ARCHIVED) {
            throw new BadRequestException("Project is archived and cannot be modified");
        }
    }

    @Transactional
    public ProjectResponse archiveProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        project.setStatus(ProjectStatus.ARCHIVED);
        project = projectRepository.save(project);
        return ProjectResponse.fromEntity(project);
    }

    @Transactional
    public ProjectResponse unarchiveProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        project.setStatus(ProjectStatus.ACTIVE);
        project = projectRepository.save(project);
        return ProjectResponse.fromEntity(project);
    }

    @Transactional
    public ProjectResponse updateProject(Long id, String name, String description,
                                          LocalDate startDate, LocalDate endDate, ProjectStatus status,
                                          BigDecimal budget, String remarks, ProjectPriority priority) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        validateProjectNotArchived(id);

        if (name != null && !name.isBlank()) {
            project.setName(name);
        }
        if (description != null) {
            project.setDescription(description);
        }
        if (startDate != null) {
            project.setStartDate(startDate);
        }
        if (endDate != null) {
            project.setEndDate(endDate);
        }
        if (status != null) {
            project.setStatus(status);
        }
        if (priority != null) {
            project.setPriority(priority);
        }
        if (budget != null) {
            project.setBudget(budget);
        }
        if (remarks != null) {
            project.setRemarks(remarks);
        }

        project = projectRepository.save(project);
        return ProjectResponse.fromEntity(project);
    }

    @Transactional
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        validateProjectNotArchived(id);
        project.setStatus(ProjectStatus.DELETED);
        projectRepository.save(project);
    }

    @Transactional
    public ProjectMember addMember(Long projectId, Long userId, MemberRole role) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        validateProjectNotArchived(projectId);

        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new BadRequestException("User is already a member of this project");
        }

        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(user);
        member.setRole(role != null ? role : MemberRole.MEMBER);

        return projectMemberRepository.save(member);
    }

    @Transactional
    public void removeMember(Long projectId, Long userId) {
        validateProjectNotArchived(projectId);
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new ResourceNotFoundException("Project member not found");
        }
        projectMemberRepository.deleteByProjectIdAndUserId(projectId, userId);
    }

    @Transactional
    public ProjectMember updateMemberRole(Long projectId, Long userId, MemberRole role) {
        validateProjectNotArchived(projectId);
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project member not found"));
        member.setRole(role);
        return projectMemberRepository.save(member);
    }

    public List<ProjectMember> getProjectMembers(Long projectId) {
        return projectMemberRepository.findByProjectId(projectId);
    }

    public boolean isProjectMember(Long projectId, Long userId) {
        return projectMemberRepository.existsByProjectIdAndUserId(projectId, userId);
    }

    public boolean isProjectMemberNotViewer(Long projectId, Long userId) {
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElse(null);
        return member != null && member.getRole() != MemberRole.VIEWER;
    }

    public boolean isProjectOwner(Long projectId, Long userId) {
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElse(null);
        return member != null && member.getRole() == MemberRole.OWNER;
    }

    public boolean hasProjectAccess(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) return false;
        if (project.getOwner().getId().equals(userId)) return true;
        return projectMemberRepository.existsByProjectIdAndUserId(projectId, userId);
    }

    public void validateProjectAccess(Long projectId, Long userId) {
        if (!hasProjectAccess(projectId, userId)) {
            throw new ForbiddenException("You don't have access to this project");
        }
    }

    public void validateProjectAdmin(Long projectId, Long userId) {
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ForbiddenException("You don't have access to this project"));
        if (member.getRole() != MemberRole.OWNER && member.getRole() != MemberRole.ADMIN) {
            throw new ForbiddenException("You need admin or owner role to perform this action");
        }
    }

    // SpEL-compatible permission helpers for @PreAuthorize

    public boolean checkProjectAccess(Long projectId, String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        return user != null && hasProjectAccess(projectId, user.getId());
    }

    public boolean checkProjectAdmin(Long projectId, String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return false;
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, user.getId()).orElse(null);
        return member != null && (member.getRole() == MemberRole.OWNER || member.getRole() == MemberRole.ADMIN);
    }

    public boolean checkProjectOwner(Long projectId, String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        return user != null && isProjectOwner(projectId, user.getId());
    }

    public boolean checkProjectAccessByCode(String code, String username) {
        Project project = projectRepository.findByCode(code).orElse(null);
        if (project == null) return false;
        return checkProjectAccess(project.getId(), username);
    }

    public boolean checkProjectMember(Long projectId, String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        return user != null && isProjectMemberNotViewer(projectId, user.getId());
    }
}
