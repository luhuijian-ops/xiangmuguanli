package com.xiangmuguanli.repository;

import com.xiangmuguanli.entity.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskDependencyRepository extends JpaRepository<TaskDependency, Long> {

    List<TaskDependency> findByTaskId(Long taskId);

    List<TaskDependency> findByDependOnTaskId(Long dependOnTaskId);

    boolean existsByTaskIdAndDependOnTaskId(Long taskId, Long dependOnTaskId);

    void deleteByTaskIdAndDependOnTaskId(Long taskId, Long dependOnTaskId);

    void deleteByTaskId(Long taskId);

    void deleteByDependOnTaskId(Long dependOnTaskId);
}
