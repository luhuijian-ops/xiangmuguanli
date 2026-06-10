package com.xiangmuguanli.repository;

import com.xiangmuguanli.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    List<UserRole> findByUserId(Long userId);

    List<UserRole> findByRoleId(Long roleId);

    List<UserRole> findByProjectId(Long projectId);

    Optional<UserRole> findByUserIdAndRoleIdAndProjectId(Long userId, Long roleId, Long projectId);

    void deleteByUserIdAndProjectId(Long userId, Long projectId);
}
