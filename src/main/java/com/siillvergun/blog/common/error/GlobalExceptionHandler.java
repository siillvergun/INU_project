package com.siillvergun.blog.common.error;

import com.siillvergun.blog.common.error.dto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // "이 서버의 모든 컨트롤러에서 발생하는 에러는 이 클래스가 관리
@Slf4j
/// 이 클래스에서 잡는 모든 에러는 애플리케이션/서비스 에러임
public class GlobalExceptionHandler {

    /// 커스텀 에러
    @ExceptionHandler(CustomException.class) // 특히 CustomException이 날라오면 잡는다
    public ResponseEntity<ErrorResponseDto> handleCustomException(CustomException e) {
        // 잡은 에러객체(e)를 열어서 안에 든 ErrorCode를 꺼냄
        ErrorCode errorCode = e.getErrorCode();
        // 에러 로그 남김
        log.error("Custom Error: {} - {}", errorCode.getCode(), errorCode.getMessage());
        // 에러를 Dto에 담는다
        ErrorResponseDto response = ErrorResponseDto.of(e.getErrorCode());
        // Http 상태 코드를 JSON 데이터의 응답으로 보냄
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    /// 입력값 에러
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationException(MethodArgumentNotValidException e) {
        // 1. DTO에서 설정한 에러 메시지 꺼내기 (예: "특수문자를 포함하여 8자 이상 입력하시오")
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        // 2. 에러 로그 남기기
        log.error("Validation Error: {}", errorMessage);
        // 3. 에러를 Dto에 담기 (새로 만든 of 메서드 사용)
        ErrorResponseDto response = ErrorResponseDto.of(ErrorCode.INVALID_INPUT_VALUE, errorMessage);
        // 4. Http 상태 코드를 JSON 데이터의 응답으로 보냄 (400 Bad Request)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /// 잘못된 HTTP 메서드 에러
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDto> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("Method Not Allowed: {}", e.getMessage());
        ErrorResponseDto response = ErrorResponseDto.of(ErrorCode.METHOD_NOT_ALLOWED);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    /// JSON 파싱 에러
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("JSON Parse Error: {}", e.getMessage());
        ErrorResponseDto response = ErrorResponseDto.of(ErrorCode.BAD_REQUEST_JSON);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /// 미처 잡지 못한 에러들
    // 전부 서버 에러로 처리
    @ExceptionHandler(Exception.class) // Exception의 최상위 클래스
    public ResponseEntity<ErrorResponseDto> handleAllException(Exception e) {
        // 500 에러는 서버 문제이므로, 개발자가 원인을 파악할 수 있도록 전체 에러 로그(e)를 남깁니다.
        log.error("Internal Server Error: ", e);

        // 프론트엔드에게는 상세한 에러 원인 대신 "서버 오류"라고만 예쁘게 포장해서 보냅니다. (보안 목적)
        ErrorResponseDto response = ErrorResponseDto.of(ErrorCode.INTERNAL_SERVER_ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
