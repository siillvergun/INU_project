package com.siillvergun.blog.post.controller;

import com.siillvergun.blog.post.dto.PostRequestDto;
import com.siillvergun.blog.post.dto.PostResponseDto;
import com.siillvergun.blog.post.service.PostService;
import com.siillvergun.blog.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor // final
public class PostController {
    private final PostService postService;
    private final UserService userService;

    ///  게시글 생성
    @PostMapping("/me")
    public ResponseEntity<PostResponseDto> createPost(
            @Valid @RequestBody PostRequestDto postRequestDto,
            Authentication authentication

    ) {
        Long userId = userService.getCurrentUserId(authentication);
        PostResponseDto response = postService.createPost(postRequestDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    /// 모든 게시글 검색
    @GetMapping
    public ResponseEntity<List<PostResponseDto>> getPosts(
    ) {
        // 전체 검색
        List<PostResponseDto> AllPosts = postService.getAllPost();
        return ResponseEntity.ok(AllPosts);
    }


    /// 내 게시글 조회
    @GetMapping("/me/user")
    public ResponseEntity<List<PostResponseDto>> getUserPosts(Authentication authentication) {
        Long userId = userService.getCurrentUserId(authentication);
        List<PostResponseDto> userPosts = postService.getPostByAuthor(userId);
        return ResponseEntity.ok(userPosts);
    }

    /// 게시글 수정
    @PatchMapping("/me/{postId}")
    public ResponseEntity<PostResponseDto> updatePost(
            @PathVariable Long postId,
            Authentication authentication,
            @RequestBody PostRequestDto updateDto) {
        Long userId = userService.getCurrentUserId(authentication);
        PostResponseDto response = postService.updatePost(postId, updateDto, userId);
        return ResponseEntity.ok(response);
    }


    /// 게시글 삭제
    @DeleteMapping("/me/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            Authentication authentication
    ) {
        Long userId = userService.getCurrentUserId(authentication);
        postService.deletePost(postId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/{postId}/like")
    public ResponseEntity<Void> togglePostLike(
            @PathVariable Long postId,
            Authentication authentication
    ) {
        Long userId = userService.getCurrentUserId(authentication);
        postService.toggleLike(userId, postId);
        return ResponseEntity.ok().build();
    }
}
