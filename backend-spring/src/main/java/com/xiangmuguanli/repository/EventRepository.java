package com.xiangmuguanli.repository;

import com.xiangmuguanli.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByUserIdAndStartTimeBetween(Long userId, LocalDateTime start, LocalDateTime end);

    List<Event> findByProjectId(Long projectId);

    List<Event> findByTaskId(Long taskId);

    List<Event> findByUserId(Long userId);
}
