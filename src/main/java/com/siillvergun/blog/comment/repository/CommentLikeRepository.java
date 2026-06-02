package com.siillvergun.blog.comment.repository;

import com.siillvergun.blog.comment.entity.Comment;
import com.siillvergun.blog.comment.entity.CommentLike;
import com.siillvergun.blog.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByUserAndComment(User user, Comment comment);

    void deleteByComment(Comment comment);
}
