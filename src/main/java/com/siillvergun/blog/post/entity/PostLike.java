package com.siillvergun.blog.post.entity;

import com.siillvergun.blog.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "post_likes",
        // 중복방지 기능
        uniqueConstraints = {
                /*
                DB에 선택된 컬럼들의 조합은 전체 테이블에서 유일해야 한다는 어노테이션, 중복 금지
                동시성 문제 때문에 if문이 아닌 DB에 직접 제약조건을 건다
                 */
                @UniqueConstraint(
                        name = "uk_post_like_user_post",
                        columnNames = {"user_id", "post_id"}
                )
        }
)
public class PostLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long likeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Builder
    public PostLike(User user, Post post) {
        this.user = user;
        this.post = post;
    }
}
