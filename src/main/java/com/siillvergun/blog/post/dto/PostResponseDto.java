package com.siillvergun.blog.post.dto;

import com.siillvergun.blog.post.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor // 스프링이 JSON 데이터를 자바 객체(DTO)로 변환할 때(Jackson 라이브러리), 기본 생성자를 사용해 객체를 먼저 생성
@AllArgsConstructor // 클래스에 @Builder를 붙이면 기본적으로 모든 인자 생성자가 필요함
public class PostResponseDto {
    private Long postId;
    private String title;
    private String content;
    private String authorNickname;
    private Long likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PostResponseDto from(Post post) {
        return PostResponseDto.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorNickname(post.getAuthor().getNickname()) // 이때 user조회 쿼리 보냄 -> 총 N개의 쿼리가 발생
                .likeCount(post.getLikeCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}