package com.siillvergun.blog.post.repository;

import com.siillvergun.blog.post.entity.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // userId로 작성자 찾기
    // @EntityGraph를 쓰면 fetch join 쿼리를 직접 안 짜도 됨.
    @EntityGraph(attributePaths = {"author"})
    List<Post> findByAuthorUserId(Long userId);

    // fetch join - N+1문제 방지
    @Query("select p from Post p join fetch p.author")
    List<Post> findAllWithAuthor();
}
