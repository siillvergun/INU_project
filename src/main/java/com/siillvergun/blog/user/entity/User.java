package com.siillvergun.blog.user.entity;

import com.siillvergun.blog.comment.entity.Comment;
import com.siillvergun.blog.common.entity.BaseEntity;
import com.siillvergun.blog.post.entity.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter // 각 필드값을 가져오는 메서드 자동 생성
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자
@Entity // 이 클래스를 Entity로 지정
@Table(name = "users") // sql예약어에 USER가 있어서 테이블명을 따로 지정. 그냥 USER는 사용 불가
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 원래 ++seq했던걸 이 어노테이션을 통해 자동으로 증가하게 만들 수 있음
    private Long userId;

    @Column(length = 40, nullable = false, unique = true)
    private String email;

    @Column(length = 15, nullable = false, unique = true)
    private String nickname;

    @Column(length = 100, nullable = false)
    private String password;

    // 영속성 전이 설정
    @OneToMany(mappedBy = "author", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "author", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // id는 DB가 자동으로 채워주기때문에 제외
    @Builder
    public User(String email, String nickname, String password) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
    }

    // @Setter를 쓰지 않고 필요한 부분만 수정 할 수 있게 전용 세터 함수를 만듬
    // 엔티티는 스스로의 상태를 보호해야하기 떄문에 검증 로직이 추가
    public void changeProfile(String nickname, String email) {
        if (nickname != null && !nickname.isBlank()) {
            this.nickname = nickname;
        }
        if (email != null && !email.isBlank()) {
            this.email = email;
        }
    }

    public void changePassword(String encryptedPassword) {
        // 보안 및 데이터 무결성을 위한 최소한의 검증
        if (encryptedPassword == null || encryptedPassword.isBlank()) {
            throw new IllegalArgumentException("암호화된 비밀번호가 유효하지 않습니다.");
        }

        this.password = encryptedPassword;
    }
}