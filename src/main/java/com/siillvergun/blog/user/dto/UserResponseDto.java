package com.siillvergun.blog.user.dto;

import com.siillvergun.blog.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/// DTO는 recode를 쓰면 더 간단하게 작성 가능

@Getter
@Builder
@NoArgsConstructor // 스프링이 JSON 데이터를 자바 객체(DTO)로 변환할 때(Jackson 라이브러리), 기본 생성자를 사용해 객체를 먼저 생성
@AllArgsConstructor // @Builder는 모든 인자 생성자가 필요함
public class UserResponseDto {
    private Long userId;
    private String email;
    private String nickname;

    // 엔티티를 DTO로 변환( "엔티티로부터(from) 만든 DTO" )
    // 이게 없으면 일일이 빌더로 수동 매핑해줘야 하는데 메서드로 만듬으로써 코드를 깔끔하게 유지 가능
    // 외부 객체를 받아서 쓰기때문에 내부 상태를 신경쓸 필요가 없음 -> static
    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }
}
