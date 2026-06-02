package com.siillvergun.blog.auth.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtTokenProvider {
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final long expiration;

    // @RequiredArgsConstructor을 쓰면 expiration이 설정값이 아니라 스프링이 bean을 주입해야하는지에 대한 문제가 생김
    // 따라서 @Value로 이 값은 설정값이라고 명시해줌. 그러므로 생성자를 따로 알맞게 생성해줘야한다.
    // 즉 @Value의 의미는 bean을 찾지말고 프로퍼티에서 꺼내 쓰라는 의미
    public JwtTokenProvider(
            JwtEncoder jwtEncoder,
            JwtDecoder jwtDecoder,
            @Value("${jwt.expiration}") long expiration
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.expiration = expiration;
    }

    public String createAccessToken(Long userId) {
        // 토큰 발급 시각, issuedAt, expiresAt의 기준이 됨
        Instant now = Instant.now();

        // 토큰 안에 들어가는 내용(payload)
        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                // String.valueOf메서드를 통해서 Long타입을 String타입으로 저장
                .subject(String.valueOf(userId)) // 토큰의 발급자
                .issuedAt(now) // 생성 시간
                .expiresAt(now.plusMillis(expiration)) // 만료 시간
                .build();

        // 어떤 서명 알고리즘을 썼는지에 대한 정보(header)
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

        // header+payload를 받고 JwtConfig에서 등록한 비밀키를 사용해서
        // signature를 만들고 최족적으로 header.payload.signature 형태의 JWT문자열을 만들어 반환한다.
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, jwtClaimsSet)).getTokenValue();
    }


    public Long parseBearerToken(String token) {
        // jwt문자열인 token을 해석해서 spring security가 다루기 쉬운 jwt객체로 바꿔줌
        // 문자열 안에 들어있던 JWT 정보를 파싱해서 객체로 복원하는 작업
        try {
            Jwt jwt = jwtDecoder.decode(token);
            // 문자열로 저장된 userId를 다시 Long형으로 복원
            return Long.valueOf(jwt.getSubject());
        } catch (JwtException e) {
            throw new IllegalArgumentException("유효하지 않은 JWT 토큰입니다");
        } catch (NumberFormatException e) {
            throw new NumberFormatException("JWT subject 형식이 올바르지 않습니다.");
        }
    }

    // 토큰을 파싱해서 Bearer를 확인하는 메서드
    public String bearerParse(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            return null;
        }
        if (!authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }
}
