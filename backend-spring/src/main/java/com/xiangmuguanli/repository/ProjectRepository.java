package com.xiangmuguanli.repository;

import com.xiangmuguanli.entity.Project;
import com.xiangmuguanli.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByCode(String code);

    List<Project> findByOwnerId(Long ownerId);

    Page<Project> findByOwnerId(Long ownerId, Pageable pageable);

    List<Project> findByStatus(ProjectStatus status);

    boolean existsByCode(String code);
}
