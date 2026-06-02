package com.siillvergun.blog.common.error.dto;

import com.siillvergun.blog.common.error.ErrorCode;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponseDto {
    private final String errorcode;
    private final String message;

    /// 그냥 에러 코드에 있는 메시지를 받음(에러코드에 있는 기본적인 메시지)
    public static ErrorResponseDto of(ErrorCode errorCode) {
        return ErrorResponseDto.builder()
                .errorcode(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }

    /// DTO에서 넘어온 메시지도 받을 수 있는 팩토리 메서드(DTO에 있는 구체적인 메시지)
    public static ErrorResponseDto of(ErrorCode errorCode, String customMessage) {
        return ErrorResponseDto.builder()
                .errorcode(errorCode.getCode())
                .message(customMessage) // DTO에서 넘어온 상세 메시지를 덮어씌움
                .build();
    }
}
