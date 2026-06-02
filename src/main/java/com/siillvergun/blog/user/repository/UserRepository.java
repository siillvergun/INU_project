package com.siillvergun.blog.user.repository;

import com.siillvergun.blog.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // JpaRepository를 상속(제네릭)
    // 규칙: find + By + 필드명
    // 작동 원리: findByEmail(String email)이라고 쓰면,
    // 스프링은 내부적으로 SELECT * FROM user_entity WHERE email = ? 라는 SQL을 생성합니다.
    // 공통 기능(save, findAll 등): 이미 들어있으니 안 써도 됨.
    // 내 필드 검색(findByEmail 등): 인터페이스에 이름만 선언하면 됨 (구현은 스프링 몫).
    // 복잡한 기능: 직접 @Query를 써서 SQL을 알려줘야 함.
    // Optional은 "값이 있을 수도 있고, 없을 수도 있는 상태"를 감싸는 일종의 상자(Wrapper)
    // 사용하는 이유는 nullpointexception을 방지하기 위해서
    Optional<User> findByEmail(String email);
}
