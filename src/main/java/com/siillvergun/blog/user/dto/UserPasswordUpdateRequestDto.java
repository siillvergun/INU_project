package com.siillvergun.blog.user.dto;

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
public class UserPasswordUpdateRequestDto {
    private String currentPassword; // 현재 비밀번호 (본인 확인용)
    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,16}$", message = "특수문자를 포함하여 8자 이상 입력하시오")
    private String newPassword;     // 새로 바꿀 비밀번호
}
