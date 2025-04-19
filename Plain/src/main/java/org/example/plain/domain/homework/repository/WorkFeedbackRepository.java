package org.example.plain.domain.homework.repository;

import org.example.plain.domain.homework.entity.WorkEntity;
import org.example.plain.domain.homework.entity.WorkFeedbackEntity;
import org.example.plain.domain.homework.entity.WorkFeedbackId;
import org.example.plain.domain.homework.entity.WorkMemberId;
import org.example.plain.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 과제 피드백 리포지토리
 */
@Repository
public interface WorkFeedbackRepository extends JpaRepository<WorkFeedbackEntity, WorkFeedbackId> {
    
    /**
     * 과제 ID와 학생 ID로 피드백 조회
     * @param workId 과제 ID
     * @param studentId 학생 ID
     * @return 피드백 옵셔널
     */
    @Query("SELECT wf FROM WorkFeedbackEntity wf WHERE wf.work.workId = :workId AND wf.user.id = :studentId")
    Optional<WorkFeedbackEntity> findByWorkIdAndStudentId(@Param("workId") String workId, @Param("studentId") String studentId);
    
    /**
     * 과제 ID로 모든 피드백 조회
     * @param workId 과제 ID
     * @return 피드백 목록
     */
    @Query("SELECT wf FROM WorkFeedbackEntity wf WHERE wf.work.workId = :workId")
    List<WorkFeedbackEntity> findByWorkId(@Param("workId") String workId);
    
    /**
     * 학생 ID로 모든 피드백 조회
     * @param studentId 학생 ID
     * @return 피드백 목록
     */
    @Query("SELECT wf FROM WorkFeedbackEntity wf WHERE wf.user.id = :studentId")
    List<WorkFeedbackEntity> findByStudentId(@Param("studentId") String studentId);
    
    /**
     * 교수 ID로 모든 피드백 조회
     * @param instructorId
     * @return 피드백 목록
     */
    List<WorkFeedbackEntity> findByInstructorId(String instructorId);
    
    /**
     * 과제 ID와 학생 ID로 피드백 삭제
     * @param workId 과제 ID
     * @param studentId 학생 ID
     */
    @Query("DELETE FROM WorkFeedbackEntity wf WHERE wf.work.workId = :workId AND wf.user.id = :studentId")
    void deleteByWorkIdAndStudentId(@Param("workId") String workId, @Param("studentId") String studentId);

}