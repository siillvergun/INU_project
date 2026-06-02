package com.siillvergun.blog.user.controller;

import com.siillvergun.blog.user.dto.*;
import com.siillvergun.blog.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
클라이언트와 직접 소통하는 계층, 따라서 민감한 정보인 entity를 넘겨줄 필요가 없으면 보안을 생각하면서 작성해야함.
따라서 모든 메서드의 리턴값은 dto를 통해서 넘겨준다.
 */
@RestController // REST API 사용하겟다는 어노테이션, API만 다루기 때문, 화면처리도 해줘야한다면 @Controller
@RequestMapping("/users") // 이 클래스 안의 메서드들이 공통으로 가질 URL 주소를 정합니다. (users를 가짐)
@RequiredArgsConstructor // final로 생성된 필드들을 매개변수로 가지는 생성자를 만들어줌
public class UserController {
    // UserService는 한 번 값을 할당 받으면 변경될 필요가 없기 때문에 final로 선언
    private final UserService userService;

    /// 회원 가입
    // @RequestBody: JSON 문자열을 자바 객체로 변환해주는 역할, 포스트맨에서 JSON 형식으로 보낸 문자열 데이터를 자바 객체로 변환
    // 내부적으로 Jackson이라는 라이브러리가 가동되어, JSON의 키(email)와 User 클래스의 필드(email)를 매칭
    @PostMapping("/join")
    public ResponseEntity<UserResponseDto> joinUser(@Valid @RequestBody UserJoinRequestDto joinRequest) {
        UserResponseDto response = userService.join(joinRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    /// 모든 유저 조회 API
    @GetMapping // 브라우저에 주소를 쳤을 때(조회 요청) 실행되는 메서드임을 나타냅니다.
    public ResponseEntity<List<PublicUserResponseDto>> getAllUsers() {
        List<PublicUserResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users); // 200ok를 넘겨줌
    }

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyInfo(Authentication authentication) {
        Long userId = userService.getCurrentUserId(authentication);
        UserResponseDto userResponseDto = userService.getUserInfo(userId);
        return ResponseEntity.ok(userResponseDto);
    }

/// 검색은 나중에 검색 엔진(알고리즘) 붙이기
//    // 다른 사용자 정보 조회
//    @GetMapping("/{userId}")
//    // @PathVariable : URL 경로에 들어있는 값(예: /users/1에서 1)을 변수로 가져올 때 사용합니다.
//    public ResponseEntity<PublicUserResponseDto> getAnotherUser(Authentication authentication) {
//        Long userId = (Long) authentication.getPrincipal();
//        PublicUserResponseDto PublicUserResponseDto = userService.getAnotherUserInfo(userId);
//        return ResponseEntity.ok(PublicUserResponseDto);
//    }


    /// 회원 정보 수정
    @PatchMapping("/me")
    public ResponseEntity<UserResponseDto> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UserProfileUpdateRequestDto updateRequest) {
        Long userId = userService.getCurrentUserId(authentication);
        UserResponseDto response = userService.updateProfile(userId, updateRequest);
        return ResponseEntity.ok(response);
    }


    /// 패스워드 수정(패스워드는 사용자에게 넘겨주지 않음)
    @PatchMapping("/me/password")
    public ResponseEntity<Void> updatePassword(
            Authentication authentication,
            @Valid @RequestBody UserPasswordUpdateRequestDto updateRequest) {
        Long userId = userService.getCurrentUserId(authentication);
        userService.updatePassword(userId, updateRequest);
        return ResponseEntity.noContent().build();
    }


    /// 회원 삭제
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(Authentication authentication) {
        Long userId = userService.getCurrentUserId(authentication);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}