package com.xiangmuguanli.repository;

import com.xiangmuguanli.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {

    List<FileEntity> findByProjectId(Long projectId);

    List<FileEntity> findByTaskId(Long taskId);

    List<FileEntity> findByUploaderId(Long userId);
}
