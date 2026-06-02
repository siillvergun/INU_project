package com.siillvergun.blog.post.entity;

import com.siillvergun.blog.comment.entity.Comment;
import com.siillvergun.blog.common.entity.BaseEntity;
import com.siillvergun.blog.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;

@Getter
// protected로 설정하면 외부에서 new를 통해 직접 호출하는 것은 막으면서, JPA(상속 관계나 프록시 객체)는 이 생성자에 접근할 수 있도록 허용해줌.
// (JPA 스펙상 기본 생성자는 public 또는 protected)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "posts")
public class Post extends BaseEntity {
    // ERD를 보면 외래키가 숫자이기 때문에 userId를 받아와야하는게 아닌가 생각했지만
    // 자바 코드 상으로는 객체를 중심으로 생각하는게 더 좋음
    // 이렇게 작성하면 다른 모든 정보에 접근 가능
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    // @ManyToOne은 여러 개의 게시글이 하나의 유저에 속한, 또한 userId가 아닌 user객체 자체를 받아서 필요한 메서드를 사용 가능
    // @ManyToOne은 기본적으로 즉시 로딩, 게시글 데이터만 먼저 가져오고 작성자 정보는 나중에 로딩
    // 지연로딩을 사용하면 N+1문제가 발생할 수 있지만 만약 그런다면 fetch join을 사용하기!
    @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn : 객체지향 모델인 엔티티에서 외래 키(Foreign Key)를 매핑할 때 사용하는 어노테이션
    @JoinColumn(name = "user_id")
    private User author;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 1000, nullable = false)
    private String content;

    @Column(nullable = false)
    @ColumnDefault("0") // DB에 기본값 0으로 설정
    private Long likeCount = 0L; // 자바 객체에서도 기본값 0

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // 게시글은 postId를 몰라도 되기 때문에 클래스에 @빌더를 다는게 아니라 생성자를 따로 만듦
    @Builder
    public Post(User author, String title, String content) {
        this.author = author;
        this.title = title;
        this.content = content;
    }

    public void updateTitle(String title) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
    }

    public void updateContent(String content) {
        if (content != null && !content.isBlank()) {
            this.content = content;
        }
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0)
            this.likeCount--;
    }
}