package com.siillvergun.blog.user.dto;

import com.siillvergun.blog.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicUserResponseDto {
    private String nickname;

    public static PublicUserResponseDto from(User AnotherUser) {
        return PublicUserResponseDto.builder()
                .nickname(AnotherUser.getNickname())
                .build();
    }
}
