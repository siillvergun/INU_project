package com.siillvergun.blog.common.error;

import lombok.Getter;

/*
이 클래스를 통해 에러 메시지와 에러 코드를 던질 수 있다
 */
@Getter
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
