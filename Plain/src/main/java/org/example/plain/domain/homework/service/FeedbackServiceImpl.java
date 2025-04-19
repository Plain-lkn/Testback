package org.example.plain.domain.homework.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.plain.domain.homework.dto.request.FeedBackRequset;
import org.example.plain.domain.homework.entity.WorkFeedbackEntity;
import org.example.plain.domain.homework.entity.WorkMemberEntity;
import org.example.plain.domain.homework.entity.WorkMemberId;
import org.example.plain.domain.homework.interfaces.FeedbackService;
import org.example.plain.domain.homework.repository.WorkFeedbackRepository;
import org.example.plain.domain.user.entity.User;
import org.example.plain.domain.user.repository.UserRepository;
import org.example.plain.repository.WorkMemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 과제 피드백 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final WorkMemberRepository workMemberRepository;
    private final WorkFeedbackRepository workFeedbackRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public void addFeedback(FeedBackRequset feedBackRequset, String userId) {
        log.info("Add feedback - workId: {}, studentId: {}", feedBackRequset.getWorkId(), feedBackRequset.getStudentId());

        User user = userRepository.findById(userId).orElseThrow(() -> 
            new HttpClientErrorException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 과제 제출 여부 확인
        WorkMemberEntity workMember = workMemberRepository.findByWorkIdAndUserId(
                feedBackRequset.getWorkId(), feedBackRequset.getStudentId())
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "과제 제출 정보를 찾을 수 없습니다"));

        // spring security 권한 인증 리팩토링 필요.
        if(!workMember.getWork().getGroup().getInstructor().equals(user)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "권한이 없습니다");
        }
        
        if (!workMember.isSubmited()) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "제출되지 않은 과제에는 피드백을 추가할 수 없습니다");
        }
        
        // 유효성 검사 - 점수 범위 (0-100)
        if (feedBackRequset.getScore() < 0 || feedBackRequset.getScore() > 100) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "점수는 0점에서 100점 사이여야 합니다");
        }
        
        // 기존 피드백이 있는지 확인
        Optional<WorkFeedbackEntity> existingFeedback = workFeedbackRepository.findByWorkIdAndStudentId(
                feedBackRequset.getWorkId(), feedBackRequset.getStudentId());
        
        WorkFeedbackEntity feedback;
        if (existingFeedback.isPresent()) {
            // 기존 피드백 수정
            feedback = existingFeedback.get();
            feedback.setFeedback(feedBackRequset.getFeedback());
            feedback.setScore(feedBackRequset.getScore());
            feedback.setUpdatedAt(LocalDateTime.now());
        } else {
            // 새로운 피드백 생성
            feedback = WorkFeedbackEntity.builder()
                    .workId(feedBackRequset.getWorkId())
                    .userId(feedBackRequset.getStudentId())
                    .work(workMember.getWork())
                    .user(workMember.getUser())
                    .instructorId(userId)
                    .feedback(feedBackRequset.getFeedback())
                    .score(feedBackRequset.getScore())
                    .build();
        }
        
        // 저장
        workFeedbackRepository.save(feedback);
        log.info(String.valueOf(workMember.getWorkMemberId()));
        
        log.info("피드백 추가 성공 - 과제ID: {}, 학생ID: {}, 점수: {}", 
                feedBackRequset.getWorkId(), feedBackRequset.getStudentId(), feedBackRequset.getScore());
    }

    @Override
    @Transactional(readOnly = true)
    public String getFeedback(String workId, String studentId) {
        log.info("Get feedback - workId: {}, studentId: {}", workId, studentId);
        
        // 과제 제출 여부 확인
        WorkMemberEntity workMember = workMemberRepository.findByWorkIdAndUserId(workId, studentId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "과제 제출 정보를 찾을 수 없습니다"));
        
        if (!workMember.isSubmited()) {
            return "과제가 제출되지 않았습니다.";
        }
        
        // 피드백 조회
        Optional<WorkFeedbackEntity> feedback = workFeedbackRepository.findByWorkIdAndStudentId(workId, studentId);
        
        return feedback.map(WorkFeedbackEntity::getFeedback).orElse("아직 피드백이 없습니다.");
    }

    @Override
    @Transactional(readOnly = true)
    public int getScore(String workId, String studentId) {
        log.info("Get score - workId: {}, studentId: {}", workId, studentId);
        
        // 과제 제출 여부 확인
        WorkMemberEntity workMember = workMemberRepository.findByWorkIdAndUserId(workId, studentId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "과제 제출 정보를 찾을 수 없습니다"));
        
        if (!workMember.isSubmited()) {
            return 0; // 미제출 과제는 0점
        }
        
        // 점수 조회
        Optional<WorkFeedbackEntity> feedback = workFeedbackRepository.findByWorkIdAndStudentId(workId, studentId);
        
        return feedback.map(WorkFeedbackEntity::getScore).orElse(0);
    }
} 