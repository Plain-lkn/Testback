package org.example.plain.domain.homework.interfaces;

import org.example.plain.domain.homework.dto.request.FeedBackRequset;
import org.springframework.transaction.annotation.Transactional;

/**
 * 과제 피드백 관련 서비스 인터페이스
 */
public interface FeedbackService {
    
    /**
     * 과제에 피드백 추가
     * @param feedBackRequset 피드백 요청 객체
     * @param userId 피드백 추가자(교수) ID
     */
    @Transactional
    void addFeedback(FeedBackRequset feedBackRequset, String userId);

    /**
     * 과제의 피드백 조회
     * @param workId 과제 ID
     * @param studentId 학생 ID
     * @return 피드백 내용
     */
    String getFeedback(String workId, String studentId);
    
    /**
     * 과제의 점수 조회
     * @param workId 과제 ID
     * @param studentId 학생 ID
     * @return 과제 점수
     */
    int getScore(String workId, String studentId);
}
