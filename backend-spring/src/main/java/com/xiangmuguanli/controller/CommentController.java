package com.xiangmuguanli.controller;

import com.xiangmuguanli.dto.response.ApiResponse;
import com.xiangmuguanli.dto.response.CommentResponse;
import com.xiangmuguanli.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @RequestParam String content,
            @RequestParam String entityType,
            @RequestParam Long entityId,
            @RequestParam Long userId,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) String mentions) {
        CommentResponse comment = commentService.createComment(content, entityType, entityId, userId, parentId, mentions);
        return ResponseEntity.ok(ApiResponse.success(comment));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CommentResponse>> getCommentById(@PathVariable Long id) {
        CommentResponse comment = commentService.getCommentById(id);
        return ResponseEntity.ok(ApiResponse.success(comment));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getCommentsByEntity(
            @PathVariable String entityType, @PathVariable Long entityId) {
        List<CommentResponse> comments = commentService.getCommentsByEntity(entityType, entityId);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }

    @GetMapping("/{id}/replies")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getReplies(@PathVariable Long id) {
        List<CommentResponse> replies = commentService.getReplies(id);
        return ResponseEntity.ok(ApiResponse.success(replies));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@commentService.isCommentOwner(#id, authentication.name)")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable Long id,
            @RequestParam String content,
            @RequestParam Long userId) {
        CommentResponse comment = commentService.updateComment(id, content, userId);
        return ResponseEntity.ok(ApiResponse.success(comment));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@commentService.isCommentOwnerOrAdmin(#id, authentication.name)")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long id, @RequestParam Long userId) {
        commentService.deleteComment(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully", null));
    }
}
