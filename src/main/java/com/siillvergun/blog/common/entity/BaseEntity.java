package com.siillvergun.blog.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass // 이 엔티티의 테이블이 DB에 생성되지 않음 -> 상속받은 엔티티의 컬럼으로 사용됨
@EntityListeners(value = {AuditingEntityListener.class})
// 엔티티의 변화(생성, 수정 등)를 감지하여 특정 로직을 실행하도록 하는 '리스너'를 등록
// 여기서 사용된 AuditingEntityListener는 엔티티가 저장되거나 수정될 때 시간을 자동으로 주입해주는 스프링 데이터 JPA의 기능을 활성화
public class BaseEntity {
    @CreatedDate // 엔티티가 처음 생성되어 저장될 때의 시간을 자동으로 기록
    @Column(name = "created_at", updatable = false)
    protected LocalDateTime createdAt;

    @LastModifiedDate //
    @Column(name = "updated_at")
    protected LocalDateTime updatedAt;
}

// DTO는 DB의 자동 시간 기록 기능(Auditing)을 알 필요가 없음
// DTO는 화면에 보여주고 싶은 정보만 골라서 담는 바구니이기 때문에 만약 baseEntity에 있는 값을 보여주고 싶다면 DTO에 필드만 추가하면 된다.
// 또한 BaseEntity에 사용된 @CreatedDate, @LastModifiedDate 같은 어노테이션들은
// JPA가 DB에 데이터를 넣고 수정할 때 자동으로 시간을 계산해주는 기능입니다. 이 기능은 JPA가 관리하는 엔티티 객체에서만 작동한다.