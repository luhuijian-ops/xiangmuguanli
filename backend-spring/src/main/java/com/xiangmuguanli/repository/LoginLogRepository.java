package com.xiangmuguanli.repository;

import com.xiangmuguanli.entity.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {

    List<LoginLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT ll FROM LoginLog ll WHERE ll.user.id = :userId AND ll.success = false AND ll.createdAt >= :since ORDER BY ll.createdAt DESC")
    List<LoginLog> findRecentFailedLogins(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(ll) FROM LoginLog ll WHERE ll.user.id = :userId AND ll.success = false AND ll.createdAt >= :since")
    long countRecentFailedLogins(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}
