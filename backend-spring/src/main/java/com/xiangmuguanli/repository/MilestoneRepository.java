package com.xiangmuguanli.repository;

import com.xiangmuguanli.entity.Milestone;
import com.xiangmuguanli.enums.MilestoneStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, Long> {

    List<Milestone> findByProjectId(Long projectId);

    List<Milestone> findByProjectIdAndStatus(Long projectId, MilestoneStatus status);

    List<Milestone> findByProjectIdOrderByOrderIndexAsc(Long projectId);
}
