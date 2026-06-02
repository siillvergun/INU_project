package com.siillvergun.blog.auth.config;

import com.siillvergun.blog.auth.jwt.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// SecurityConfig에서 선언한 BCryptPasswordEncoder는 외부 라이브러리에 있는 클래스. 따라서 내 클래스로 가져와 객체로 만들어둠.
// 이때, 외부 라이브러리에 대한 어노테이션을 직접 붙이지 못하니까, @Bean을 통해 이 메서드가 실행돼서 나오는 이 객체를 스프링이 관리하게 만든다.

@Configuration // 이 클래스는 설정 파일임을 스프링에게 알려줌. 이 안에 정의된 메서드(@Bean)들을 스프링이 읽어서 관리하게 됨.
@EnableWebSecurity // 스프링 시큐리티의 웹 보안 기능을 활성화하는 어노테이션
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtFilter jwtFilter;

    @Bean // 외부에서 가져온 객체를 스프링이 관리하게 시키는 어노테이션
    // 이 메서드를 통해 비밀번호를 암호화
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    // 어떤 요청을 허용할지, 어떤 요청을 막을지, 어떤 인증 방식을 쓸지, 어떤 필터를 넣을지
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // REST API 서버는 보통 세션을 쓰지 않고 토큰(JWT 등)을 쓰기 때문에, 테스트 편의를 위해 CSRF(사이트 간 요청 위조) 방어 기능을 잠시 꺼두는 것
        http.csrf(csrf -> csrf.disable())
                // H2 콘솔은 iframe을 사용하므로 sameOrigin 허용
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        // JWT는 세션 기반이 아니라 토큰 기반이므로 STATELESS
        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        // 회원가입, 로그인, H2 콘솔은 인증 없이 허용
        // 나머지 요청은 인증 필요
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/users/join", "/auth/login", "/h2-console/**", "/users", "/posts", "/comments").permitAll()
                .requestMatchers("/", "/index.html", "/static/**", "/css/**", "/js/**").permitAll()
                .anyRequest().authenticated()
        );

        // 기본 로그인 폼 끄기
        http.formLogin(form -> form.disable());
        // Basic 인증 끄기
        http.httpBasic(basic -> basic.disable());
        // JWT 필터를 UsernamePasswordAuthenticationFilter보다 먼저 실행
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}