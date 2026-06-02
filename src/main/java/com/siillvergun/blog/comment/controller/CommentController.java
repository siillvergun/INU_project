package com.siillvergun.blog.comment.controller;

import com.siillvergun.blog.comment.dto.CommentRequestDto;
import com.siillvergun.blog.comment.dto.CommentResponseDto;
import com.siillvergun.blog.comment.service.CommentService;
import com.siillvergun.blog.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final UserService userService;

    @PostMapping("/me/{postId}")
    public ResponseEntity<CommentResponseDto> createComment(
            @RequestBody CommentRequestDto commentRequestDto,
            @PathVariable Long postId,
            Authentication authentication
    ) {
        Long userId = userService.getCurrentUserId(authentication);
        CommentResponseDto response = commentService.createComment(commentRequestDto, userId, postId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping
    public ResponseEntity<List<CommentResponseDto>> getAllComment(
    ) {
        List<CommentResponseDto> response = commentService.getAllComment();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentRequestDto commentRequestDto,
            Authentication authentication
    ) {
        Long userId = userService.getCurrentUserId(authentication);
        CommentResponseDto response = commentService.updateComment(commentId, commentRequestDto.getContent(), userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId, Authentication authentication) {
        Long userId = userService.getCurrentUserId(authentication);
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/me/{commentId}/like")
    public ResponseEntity<Void> toggleCommentLike(
            @PathVariable Long commentId,
            Authentication authentication
    ) {
        Long userId = userService.getCurrentUserId(authentication);
        commentService.toggleLike(userId, commentId);
        return ResponseEntity.ok().build();
    }
}
