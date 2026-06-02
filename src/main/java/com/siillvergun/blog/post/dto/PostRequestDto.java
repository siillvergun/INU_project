package com.siillvergun.blog.post.dto;

import com.siillvergun.blog.post.entity.Post;
import com.siillvergun.blog.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
// 요청Dto는 클라이언트가 서버에게 처리를 요청할 떄 보내는 데이터이다.
public class PostRequestDto {
    private String title;
    private String content;

    public Post toEntity(User author) {
        return Post.builder()
                .title(this.title)
                .content(this.content)
                .author(author)
                .build();
    }
}
