package com.siillvergun.blog.user.dto;

import com.siillvergun.blog.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserJoinRequestDto {
    // 가입 시 필요한 딱 3가지 정보만 정의, id는 스프링에서 자동으로 생성
    private String email;
    private String nickname;
    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,16}$", message = "특수문자를 포함하여 8자 이상 입력하시오")
    private String password;

    // DTO를 엔티티로 변환해주는 편의 메서드
    // from()이랑 마찬가지
    // this로 참조하기 때문에 객체가 있어야한다. 따라서 static이 아닌 인스턴스 메서드로 작성
    public User toEntity(String encodedPassword) {
        return User.builder()
                .email(this.email)
                .nickname(this.nickname)
                .password(encodedPassword) // 서비스 계층에서 암호화된 비밀번호를 전달받아 엔티티 생성
                .build();
    }
}