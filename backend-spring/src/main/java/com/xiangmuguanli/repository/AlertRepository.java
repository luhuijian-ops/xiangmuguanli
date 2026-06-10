package com.xiangmuguanli.repository;

import com.xiangmuguanli.entity.Alert;
import com.xiangmuguanli.enums.AlertType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    Page<Alert> findByTypeOrderByCreatedAtDesc(AlertType type, Pageable pageable);

    Page<Alert> findByResolvedOrderByCreatedAtDesc(Boolean resolved, Pageable pageable);

    List<Alert> findByUserIdAndCreatedAtAfter(Long userId, LocalDateTime since);

    Page<Alert> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
