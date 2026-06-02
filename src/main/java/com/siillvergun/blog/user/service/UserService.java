package com.siillvergun.blog.user.service;

import com.siillvergun.blog.common.error.CustomException;
import com.siillvergun.blog.common.error.ErrorCode;
import com.siillvergun.blog.user.dto.*;
import com.siillvergun.blog.user.entity.User;
import com.siillvergun.blog.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 비즈니스 로직을 담는 곳
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // 토큰에서 userId 가져오는 메서드
    public Long getCurrentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }

    /// Create(Request DTO)
    @Transactional
    public UserResponseDto join(UserJoinRequestDto joinRequest) {
        // 평문 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(joinRequest.getPassword());

        // 매개변수로 받은 DTO에서 암호화된 패스워드를 만들고 다시 엔티티로 변환할때 평문 비밀번호가 아닌 암호화된 패스워드를 넘겨줌
        User user = joinRequest.toEntity(encodedPassword);
        return UserResponseDto.from(userRepository.save(user)); // 이제 포스트맨 응답에 비밀번호가 사라짐
    }

    /// Read(조회)
    // 전체 조회
    public List<PublicUserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream(). // 컬렉션을 스트림으로 변환 (스트림이란 데이터 소스를 추상화하여 무슨 데이터인지 상관하지않고 같은 방법으로 처리가능 )
                map(PublicUserResponseDto::from). // 각 User 객체를 UserResponse로 변환, [ stream().map(클래스명::메서드명) ]
                toList(); // 그 결과를 리스트로 모음
    }

    // 단건 조회
    // DTO는 외부와 데이터를 주고 받을 때에만 사용
    public UserResponseDto getUserInfo(Long userId) {
        User user = findUserById(userId);
        return UserResponseDto.from(user);
    }

// 검색은 나중에 알고리즘 붙이기, 지금 구현 X
//    public PublicUserResponseDto getAnotherUserInfo(Long userId) {
//        User AnotherUser = findUserById(userId);
//        return PublicUserResponseDto.from(AnotherUser);
//    }


    // 백엔드 내부에서는 엔티티를 가지고 데이터를 관리하는게 좋기 때문에 메서드 분리
    // JPA는 Entity를 관리하기 때문에 이 객체의 값이 바뀔 때 트랜젝션이 끝날 때 JPA가 자동으로 DB에 반영해줌(JPA는 DTO를 신경쓰지 않음)
    // 메서드 내에서 서버를 위한 유저 검색 메서드
    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    /// Update(갱신, DTO)
    // 프로필 업데이트
    @Transactional // 값 변경을 감지하는 어노테이션
    public UserResponseDto updateProfile(Long id, UserProfileUpdateRequestDto updateRequest) {
        // 1. DB에서 기존 유저를 조회
        User user = findUserById(id);

        // 2. 도메인 메서드를 통해 필드를 수정 (Dirty Checking 발생)
        // update에서는 엔티티로 변환 안해도 됨. join같은 경우는 새로운 객체를 만들기 때문에 엔티티로 변환하지만
        // update는 이미 JPA가 해당 객체를 관리하고 있으므로 그냥 메서드를 통해 수정이 필요한 필드만 수정하면 된다.
        // @Transactional이 값의 변경을 감지해 처리
        user.changeProfile(updateRequest.getNickname(), updateRequest.getEmail());

        // 3. 보안을 위해 엔티티 대신 응답 전용 DTO로 변환하여 반환
        return UserResponseDto.from(user);
    }

    // 패스워드 업데이트
    @Transactional
    public void updatePassword(Long id, UserPasswordUpdateRequestDto updateRequest) {
        User user = findUserById(id);

        // 1. 현재 비밀번호가 일치하는지 확인
        // passwordEncoder.matches(평문, 암호문) 메서드를 사용합니다.
        if (!passwordEncoder.matches(updateRequest.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 2. 새 비밀번호를 암호화하여 저장
        String encryptedPassword = passwordEncoder.encode(updateRequest.getNewPassword());
        user.changePassword(encryptedPassword);
        log.info("비밀번호 변경 완료 - 사용자 ID: {}", id);
    }

    /// Delete(삭제)
    @Transactional
    public void deleteUser(Long id) {
        User user = findUserById(id);

        // 2. 삭제 실행
        log.warn("사용자 삭제 실행 - ID: {}", id);
        userRepository.delete(user);
    }
}