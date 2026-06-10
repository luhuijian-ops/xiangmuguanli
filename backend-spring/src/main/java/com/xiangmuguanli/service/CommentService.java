package com.xiangmuguanli.service;

import com.xiangmuguanli.dto.response.CommentResponse;
import com.xiangmuguanli.entity.Comment;
import com.xiangmuguanli.entity.User;
import com.xiangmuguanli.exception.ResourceNotFoundException;
import com.xiangmuguanli.repository.CommentRepository;
import com.xiangmuguanli.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CommentResponse createComment(String content, String entityType, Long entityId,
                                           Long userId, Long parentId, String mentions) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setEntityType(entityType);
        comment.setEntityId(entityId);
        comment.setUser(user);
        comment.setParentId(parentId);
        comment.setMentions(mentions);

        comment = commentRepository.save(comment);
        return CommentResponse.fromEntity(comment);
    }

    public CommentResponse getCommentById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));
        return CommentResponse.fromEntity(comment);
    }

    public List<CommentResponse> getCommentsByEntity(String entityType, Long entityId) {
        return commentRepository.findByEntityTypeAndEntityIdAndParentIdIsNull(entityType, entityId)
                .stream()
                .map(CommentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<CommentResponse> getReplies(Long parentId) {
        return commentRepository.findByParentId(parentId)
                .stream()
                .map(CommentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponse updateComment(Long id, String content, Long userId) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));

        if (!comment.getUser().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("You can only edit your own comments");
        }

        comment.setContent(content);
        comment = commentRepository.save(comment);
        return CommentResponse.fromEntity(comment);
    }

    @Transactional
    public void deleteComment(Long id, Long userId) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));

        if (!comment.getUser().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }

    // SpEL-compatible permission helpers for @PreAuthorize

    public boolean isCommentOwner(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) return false;
        return comment.getUser().getUsername().equals(username);
    }

    public boolean isCommentOwnerOrAdmin(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) return false;
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return false;
        if (user.isAdmin()) return true;
        return comment.getUser().getId().equals(user.getId());
    }
}
