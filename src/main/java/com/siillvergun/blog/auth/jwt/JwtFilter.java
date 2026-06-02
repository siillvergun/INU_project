package com.siillvergun.blog.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component // 스프링 빈으로 등록
@RequiredArgsConstructor
// OncePerRequestFilter는 요청당 한 번만 실행되도록 보장해주는 필터 베이스 클래스
// JwtFilter가 OncePerRequestFilter를 상속하는 이유는 **“같은 요청에 대해 JWT 인증 로직이 여러 번 돌지 않게 하기 위함
public class JwtFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    // doFilter와 같은 계약이지만, 한 요청의 한 스레드 안에서 한 번만 호출되도록 보장되는 메서드
    // HttpServletRequest/HttpServletResponse를 바로 받게 해줌
    @Override // OncePerRequestFilter에 있는 doFilterInternal()을 오버라이드
    protected void doFilterInternal(
            HttpServletRequest httpServletRequest, // 들어온 http요청 정보
            HttpServletResponse httpServletResponse, // 서버가 클라이언에게 보낼 http응답 정보
            FilterChain filterChain // 컨트롤러나 다음 필터에게 요청을 넘기를 통로(없으면 요청이 중간에 멈출 수 있음)
    ) throws ServletException, IOException {
        // 요청 헤더에서 Authorization 값을 꺼냄(Bearer + JWT문자열)
        String authHeader = httpServletRequest.getHeader("Authorization");
        // Bearer를 땐 순수 JWT 문자열
        String token = jwtTokenProvider.bearerParse(authHeader);

        if (token != null) {
            try {
                // userId를 JWT문자열에서 파싱
                Long userId = jwtTokenProvider.parseBearerToken(token);
                // 스프링 시큐리티용 인증 객체 생성
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userId, // principal: “현재 로그인 사용자가 누구냐”를 나타내는 핵심값"
                        null, // credentials: 보통 비밀번호 같은 민감한 정보, JWT 인증이 끝난 뒤에는 다시 들고 있을 필요가 없어서 null로 설정
                        Collections.emptyList() // 권한 목록(authorities): 지금은 역할(ROLE_USER, ROLE_ADMIN) 같은 걸 아직 안 쓰니까 빈 리스트로 둠
                );
                // 토큰에서 꺼낸 userId를 Spring Security에 등록
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }

        // 다음 필터에게 http응답,요청을 매개변수로 보내서 요청이 계속되게 함
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
