package com.siillvergun.blog.auth.service;

import com.siillvergun.blog.auth.dto.LoginRequestDto;
import com.siillvergun.blog.auth.dto.LoginResponseDto;
import com.siillvergun.blog.auth.jwt.JwtTokenProvider;
import com.siillvergun.blog.user.entity.User;
import com.siillvergun.blog.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /// Login(로그인)
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        String email = loginRequestDto.getEmail();
        String password = loginRequestDto.getPassword();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        // passwordEncoder.matches(평문비밀번호, 암호화된비밀번호)
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId());
        return new LoginResponseDto(accessToken);
    }
}
