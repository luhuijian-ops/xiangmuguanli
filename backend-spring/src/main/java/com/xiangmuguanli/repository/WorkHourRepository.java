package com.xiangmuguanli.repository;

import com.xiangmuguanli.entity.WorkHour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkHourRepository extends JpaRepository<WorkHour, Long> {

    List<WorkHour> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    List<WorkHour> findByProjectId(Long projectId);

    List<WorkHour> findByTaskId(Long taskId);

    List<WorkHour> findByUserId(Long userId);
}
