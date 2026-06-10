package com.xiangmuguanli.repository;

import com.xiangmuguanli.entity.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    Page<Activity> findByProjectIdOrderByCreatedAtDesc(Long projectId, Pageable pageable);

    List<Activity> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<Activity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Activity> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId, Pageable pageable);

    @Query("SELECT a FROM Activity a WHERE " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:startDate IS NULL OR a.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR a.createdAt <= :endDate) " +
           "ORDER BY a.createdAt DESC")
    Page<Activity> findAuditLogs(@Param("action") String action,
                                  @Param("startDate") String startDate,
                                  @Param("endDate") String endDate,
                                  Pageable pageable);
}
