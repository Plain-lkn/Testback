package org.example.plain.domain.notice.repository;

import org.example.plain.domain.notice.entity.NoticeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NoticeRepository extends JpaRepository<NoticeEntity, String> {
    
    @Query("SELECT n FROM NoticeEntity n JOIN FETCH n.user JOIN FETCH n.classLecture")
    List<NoticeEntity> findAllWithUserAndClassLecture();
}
