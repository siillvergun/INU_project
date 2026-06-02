package com.siillvergun.blog.comment.entity;

import com.siillvergun.blog.common.entity.BaseEntity;
import com.siillvergun.blog.post.entity.Post;
import com.siillvergun.blog.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "comments")
public class Comment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User author;

    @Column(length = 100)
    private String content;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Long likeCount = 0L;

    @Builder
    public Comment(String content, Post post, User author) {
        this.author = author;
        this.post = post;
        this.content = content;
    }

    public void changeComment(String content) {
        if (content != null && !content.isBlank())
            this.content = content;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0)
            this.likeCount--;
    }
}
