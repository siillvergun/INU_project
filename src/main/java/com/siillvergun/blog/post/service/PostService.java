package com.siillvergun.blog.post.service;

import com.siillvergun.blog.common.error.CustomException;
import com.siillvergun.blog.common.error.ErrorCode;
import com.siillvergun.blog.post.dto.PostRequestDto;
import com.siillvergun.blog.post.dto.PostResponseDto;
import com.siillvergun.blog.post.entity.Post;
import com.siillvergun.blog.post.entity.PostLike;
import com.siillvergun.blog.post.repository.PostLikeRepository;
import com.siillvergun.blog.post.repository.PostRepository;
import com.siillvergun.blog.user.entity.User;
import com.siillvergun.blog.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.siillvergun.blog.common.error.ErrorCode.POST_NOT_FOUND;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PostService {
    private final PostRepository postRepository;
    private final UserService userService;
    private final PostLikeRepository postLikeRepository;

    /// create
    @Transactional // 쓰기 작업이 있는 메서드만
    public PostResponseDto createPost(PostRequestDto postRequestDto, Long userId) {
        User author = userService.findUserById(userId);

        Post post = postRequestDto.toEntity(author);
        return PostResponseDto.from(postRepository.save(post));
    }


    /// read
    // 게시글 전체 조회
    public List<PostResponseDto> getAllPost() {
        List<Post> posts = postRepository.findAllWithAuthor();

        return posts.stream(). // 컬렉션을 스트림으로 변환 (스트림이란 데이터 소스를 추상화하여 무슨 데이터인지 상관하지않고 같은 방법으로 처리가능 )
                map(PostResponseDto::from). // 각 User 객체를 UserResponse로 변환, [ stream().map(클래스명::메서드명) ]
                toList(); // 그 결과를 리스트로 모음
    }

    // 작성자로 조회
    public List<PostResponseDto> getPostByAuthor(Long userId) {
        List<Post> postsByUserId = postRepository.findByAuthorUserId(userId);

        return postsByUserId.stream()
                .map(PostResponseDto::from)
                .toList();
    }

    // postRepository.findById()은 반환값이 Optional, 또한 내부 로직에서는 DTO보다 Entity를 직접 다루는게 좋음
    // Post를 반환값으로 가지는 메서드를 정의
    public Post findByPostId(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(POST_NOT_FOUND));
    }

    // 작성자 검증 로직
    private void authAccess(Post post, Long userId) {
        if (!post.getAuthor().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    /// update
    @Transactional
    public PostResponseDto updatePost(Long postId, PostRequestDto updateDto, Long userId) {
        Post post = findByPostId(postId);

        authAccess(post, userId);

        post.updateTitle(updateDto.getTitle());
        post.updateContent(updateDto.getContent());
        return PostResponseDto.from(post);
    }

    /// delete
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = findByPostId(postId);

        authAccess(post, userId);

        postLikeRepository.deleteByPost(post);

        postRepository.deleteById(postId);
        log.warn("게시글 삭제 실행 - ID: {}", postId);
    }

    @Transactional
    public void toggleLike(Long userId, Long postId) {
        // 1. 게시글과 유저 존재 확인
        Post post = findByPostId(postId);
        User user = userService.findUserById(userId);

        // 2. 이미 좋아요를 눌렀는지 확인
        Optional<PostLike> optionalLike = postLikeRepository.findByUserAndPost(user, post);

        if (optionalLike.isPresent()) {
            // [CASE 1] 이미 눌렀다면 -> 좋아요 취소
            postLikeRepository.delete(optionalLike.get()); // 1. like 테이블에서 삭제
            post.decreaseLikeCount(); // 2. post 테이블의 카운트 -1 (Dirty Checking)
            log.info("게시글 좋아요 취소 - user: {}, comment: {}", userId, postId);
        } else {
            // [CASE 2] 안 눌렀다면 -> 좋아요 등록
            postLikeRepository.save(new PostLike(user, post)); // 1. like 테이블에 저장
            post.increaseLikeCount(); // 2. post 테이블의 카운트 +1 (Dirty Checking)
            log.info("게시글 좋아요 등록 - user: {}, comment: {}", userId, postId);
        }
    }
}
