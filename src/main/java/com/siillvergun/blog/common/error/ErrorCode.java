package com.siillvergun.blog.common.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/*
Http상태 및 메시지를 가지고 있는 에러 코드 상수
에러 처리 가장 기본이 되는 에러들을 커스텀하는 클래스
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND("4040", "유저를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    POST_NOT_FOUND("4041", "게시글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_ACCESS("4042", "허용되지 않은 접근입니다.", HttpStatus.NOT_FOUND),
    INVALID_INPUT_VALUE("4000", "입력값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    METHOD_NOT_ALLOWED("4050", "지원하지 않는 HTTP 메서드입니다.", HttpStatus.METHOD_NOT_ALLOWED),
    BAD_REQUEST_JSON("4001", "요청 본문(JSON) 형식이 잘못되었습니다.", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR("5000", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    NO_LONGIN_REQUEST("401", "인증되지 않은 요청입니다", HttpStatus.UNAUTHORIZED);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
