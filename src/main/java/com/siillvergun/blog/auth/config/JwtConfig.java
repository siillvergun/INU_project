package com.siillvergun.blog.auth.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration // 이 클래스는 성정 클래스
public class JwtConfig {
    // application.yml에 있는 비밀키 값 가져오고, 문자열 비밀키를 실제 jwt서명에 쓸 비밀키로 바꿈
    @Value("${jwt.secret}")
    private String secret;

    @Bean
    public SecretKey jwtSecretKey() {
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    // JWT를 compact 형식으로 인코딩하는 역할, JWT토큰을 만들 수 있는 도구
    // 토큰을 만들 때 필요한(입력한) claim값들을 모아 [header.payload.signature]구조의 JWT 문자열로 만들어줌
    @Bean
    public JwtEncoder jwtEncoder(SecretKey secretKey) {
        // 구현체로 NimbusJwtEncoder를 제공. NimbusJwtEncoder는 내부적으로 Nimbus JOSE + JWT SDK를 사용
        // JWT를 만들 때 쓸 실제 구현체로 NimbusJwtEncoder를 스프링에 등록
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
    }

    // JWT해독기
    // 서명이 맞는지, 토큰 형식이 맞는지, 만료됐는지 등을 확인
    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        // Decoder도 Encoder와 마찬가지
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey).build();
    }
}
