package com.xiangmuguanli.repository;

import com.xiangmuguanli.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByEntityTypeAndEntityId(String entityType, Long entityId);

    List<Comment> findByUserId(Long userId);

    List<Comment> findByParentId(Long parentId);

    List<Comment> findByEntityTypeAndEntityIdAndParentIdIsNull(String entityType, Long entityId);
}
