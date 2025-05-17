package org.example.plain.domain.notice.repository;

import org.example.plain.domain.notice.entity.NoticeCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeCommentRepository extends JpaRepository<NoticeCommentEntity, String> {

}
