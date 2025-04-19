package org.example.plain.domain.homework.service;

import org.example.plain.domain.homework.dto.request.FeedBackRequset;
import org.example.plain.domain.homework.entity.WorkEntity;
import org.example.plain.domain.homework.entity.WorkFeedbackEntity;
import org.example.plain.domain.homework.entity.WorkMemberEntity;
import org.example.plain.domain.homework.entity.WorkMemberId;
import org.example.plain.domain.homework.repository.WorkFeedbackRepository;
import org.example.plain.domain.user.entity.User;
import org.example.plain.domain.user.repository.UserRepository;
import org.example.plain.repository.WorkMemberRepository;
import org.example.plain.domain.classLecture.entity.ClassLecture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceImplTest {

    @Mock
    private WorkMemberRepository workMemberRepository;

    @Mock
    private WorkFeedbackRepository workFeedbackRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FeedbackServiceImpl feedbackService;

    private static final String WORK_ID = "work-1";
    private static final String STUDENT_ID = "student-1";
    private static final String INSTRUCTOR_ID = "instructor-1";
    private static final String FEEDBACK_TEXT = "Good job!";
    private static final int SCORE = 85;

    private User student;
    private User instructor;
    private WorkEntity work;
    private ClassLecture classLecture;
    private WorkMemberEntity workMember;
    private WorkFeedbackEntity workFeedback;
    private FeedBackRequset feedbackRequest;

    @BeforeEach
    void setUp() {
        // 학생 생성
        student = User.builder()
                .id(STUDENT_ID)
                .username("Test Student")
                .email("student@example.com")
                .build();

        // 강사 생성
        instructor = User.builder()
                .id(INSTRUCTOR_ID)
                .username("Test Instructor")
                .email("instructor@example.com")
                .build();

        // 클래스 생성
        classLecture = ClassLecture.builder()
                .id("class-1")
                .title("Test Class")
                .instructor(instructor)
                .build();

        // 과제 생성
        work = WorkEntity.builder()
                .workId(WORK_ID)
                .title("Test Work")
                .group(classLecture)
                .build();

        // 과제 멤버 생성
        WorkMemberId workMemberId = new WorkMemberId(WORK_ID, STUDENT_ID);
        workMember = WorkMemberEntity.builder()
                .workMemberId(workMemberId)
                .work(work)
                .user(student)
                .isSubmited(true)  // 제출됨
                .build();

        // 피드백 생성
        workFeedback = WorkFeedbackEntity.builder()
                .workId(WORK_ID)
                .userId(STUDENT_ID)
                .work(work)
                .user(student)
                .instructorId(INSTRUCTOR_ID)
                .feedback(FEEDBACK_TEXT)
                .score(SCORE)
                .build();

        // 피드백 요청 객체 생성
        feedbackRequest = new FeedBackRequset(WORK_ID, STUDENT_ID, FEEDBACK_TEXT, SCORE);
    }

    @Test
    @DisplayName("성공적으로 피드백 추가")
    void addFeedback_Success() {
        // given
        when(userRepository.findById(INSTRUCTOR_ID)).thenReturn(Optional.of(instructor));
        when(workMemberRepository.findByWorkIdAndUserId(WORK_ID, STUDENT_ID)).thenReturn(Optional.of(workMember));
        when(workFeedbackRepository.findByWorkIdAndStudentId(WORK_ID, STUDENT_ID)).thenReturn(Optional.empty());
        when(workFeedbackRepository.save(any(WorkFeedbackEntity.class))).thenReturn(workFeedback);

        // when
        feedbackService.addFeedback(feedbackRequest, INSTRUCTOR_ID);

        // then
        verify(workFeedbackRepository).save(any(WorkFeedbackEntity.class));
    }

    @Test
    @DisplayName("기존 피드백 수정")
    void updateFeedback_Success() {
        // given
        when(userRepository.findById(INSTRUCTOR_ID)).thenReturn(Optional.of(instructor));
        when(workMemberRepository.findByWorkIdAndUserId(WORK_ID, STUDENT_ID)).thenReturn(Optional.of(workMember));
        when(workFeedbackRepository.findByWorkIdAndStudentId(WORK_ID, STUDENT_ID)).thenReturn(Optional.of(workFeedback));
        when(workFeedbackRepository.save(any(WorkFeedbackEntity.class))).thenReturn(workFeedback);

        // 수정된 피드백 요청 생성
        FeedBackRequset updatedRequest = new FeedBackRequset(WORK_ID, STUDENT_ID, "Updated feedback", 90);

        // when
        feedbackService.addFeedback(updatedRequest, INSTRUCTOR_ID);

        // then
        verify(workFeedbackRepository).save(any(WorkFeedbackEntity.class));
        // 피드백이 업데이트되었는지 확인하기 위해 captureArgument 사용
        verify(workFeedbackRepository).save(argThat(feedback -> 
            "Updated feedback".equals(feedback.getFeedback()) && 
            feedback.getScore() == 90
        ));
    }

    @Test
    @DisplayName("사용자를 찾을 수 없을 때 예외 발생")
    void addFeedback_UserNotFound() {
        // given
        when(userRepository.findById(INSTRUCTOR_ID)).thenReturn(Optional.empty());

        // when & then
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> 
            feedbackService.addFeedback(feedbackRequest, INSTRUCTOR_ID)
        );
        
        assertThat(exception.getMessage()).contains("사용자를 찾을 수 없습니다");
        verify(workFeedbackRepository, never()).save(any());
    }

    @Test
    @DisplayName("과제 제출 정보를 찾을 수 없을 때 예외 발생")
    void addFeedback_WorkMemberNotFound() {
        // given
        when(userRepository.findById(INSTRUCTOR_ID)).thenReturn(Optional.of(instructor));
        when(workMemberRepository.findByWorkIdAndUserId(WORK_ID, STUDENT_ID)).thenReturn(Optional.empty());

        // when & then
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> 
            feedbackService.addFeedback(feedbackRequest, INSTRUCTOR_ID)
        );
        
        assertThat(exception.getMessage()).contains("과제 제출 정보를 찾을 수 없습니다");
        verify(workFeedbackRepository, never()).save(any());
    }

    @Test
    @DisplayName("권한이 없는 사용자가 피드백 추가 시 예외 발생")
    void addFeedback_Unauthorized() {
        // given
        User otherInstructor = User.builder()
                .id("other-instructor")
                .username("Other Instructor")
                .build();
                
        when(userRepository.findById("other-instructor")).thenReturn(Optional.of(otherInstructor));
        when(workMemberRepository.findByWorkIdAndUserId(WORK_ID, STUDENT_ID)).thenReturn(Optional.of(workMember));

        // when & then
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> 
            feedbackService.addFeedback(feedbackRequest, "other-instructor")
        );
        
        assertThat(exception.getMessage()).contains("권한이 없습니다");
        verify(workFeedbackRepository, never()).save(any());
    }

    @Test
    @DisplayName("제출되지 않은 과제에 피드백 추가 시 예외 발생")
    void addFeedback_NotSubmitted() {
        // given
        // 제출되지 않은 과제 멤버 생성
        WorkMemberEntity notSubmittedWorkMember = WorkMemberEntity.builder()
                .workMemberId(workMember.getWorkMemberId())
                .work(work)
                .user(student)
                .isSubmited(false)  // 제출 안됨
                .build();
                
        when(userRepository.findById(INSTRUCTOR_ID)).thenReturn(Optional.of(instructor));
        when(workMemberRepository.findByWorkIdAndUserId(WORK_ID, STUDENT_ID)).thenReturn(Optional.of(notSubmittedWorkMember));

        // when & then
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> 
            feedbackService.addFeedback(feedbackRequest, INSTRUCTOR_ID)
        );
        
        assertThat(exception.getMessage()).contains("제출되지 않은 과제에는 피드백을 추가할 수 없습니다");
        verify(workFeedbackRepository, never()).save(any());
    }

    @Test
    @DisplayName("유효하지 않은 점수 범위로 피드백 추가 시 예외 발생")
    void addFeedback_InvalidScore() {
        // given
        when(userRepository.findById(INSTRUCTOR_ID)).thenReturn(Optional.of(instructor));
        when(workMemberRepository.findByWorkIdAndUserId(WORK_ID, STUDENT_ID)).thenReturn(Optional.of(workMember));
        
        // 유효하지 않은 점수(101점)로 피드백 요청 생성
        FeedBackRequset invalidScoreRequest = new FeedBackRequset(WORK_ID, STUDENT_ID, FEEDBACK_TEXT, 101);

        // when & then
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> 
            feedbackService.addFeedback(invalidScoreRequest, INSTRUCTOR_ID)
        );
        
        assertThat(exception.getMessage()).contains("점수는 0점에서 100점 사이여야 합니다");
        verify(workFeedbackRepository, never()).save(any());
    }

    @Test
    @DisplayName("피드백 조회 성공")
    void getFeedback_Success() {
        // given
        when(workMemberRepository.findByWorkIdAndUserId(WORK_ID, STUDENT_ID)).thenReturn(Optional.of(workMember));
        when(workFeedbackRepository.findByWorkIdAndStudentId(WORK_ID, STUDENT_ID)).thenReturn(Optional.of(workFeedback));

        // when
        String feedback = feedbackService.getFeedback(WORK_ID, STUDENT_ID);

        // then
        assertThat(feedback).isEqualTo(FEEDBACK_TEXT);
    }

    @Test
    @DisplayName("제출되지 않은 과제의 피드백 조회")
    void getFeedback_NotSubmitted() {
        // given
        // 제출되지 않은 과제 멤버 생성
        WorkMemberEntity notSubmittedWorkMember = WorkMemberEntity.builder()
                .workMemberId(workMember.getWorkMemberId())
                .work(work)
                .user(student)
                .isSubmited(false)  // 제출 안됨
                .build();
                
        when(workMemberRepository.findByWorkIdAndUserId(WORK_ID, STUDENT_ID)).thenReturn(Optional.of(notSubmittedWorkMember));

        // when
        String feedback = feedbackService.getFeedback(WORK_ID, STUDENT_ID);

        // then
        assertThat(feedback).isEqualTo("과제가 제출되지 않았습니다.");
    }

    @Test
    @DisplayName("피드백이 없는 과제의 피드백 조회")
    void getFeedback_NoFeedback() {
        // given
        when(workMemberRepository.findByWorkIdAndUserId(WORK_ID, STUDENT_ID)).thenReturn(Optional.of(workMember));
        when(workFeedbackRepository.findByWorkIdAndStudentId(WORK_ID, STUDENT_ID)).thenReturn(Optional.empty());

        // when
        String feedback = feedbackService.getFeedback(WORK_ID, STUDENT_ID);

        // then
        assertThat(feedback).isEqualTo("아직 피드백이 없습니다.");
    }

    @Test
    @DisplayName("점수 조회 성공")
    void getScore_Success() {
        // given
        when(workMemberRepository.findByWorkIdAndUserId(WORK_ID, STUDENT_ID)).thenReturn(Optional.of(workMember));
        when(workFeedbackRepository.findByWorkIdAndStudentId(WORK_ID, STUDENT_ID)).thenReturn(Optional.of(workFeedback));

        // when
        int score = feedbackService.getScore(WORK_ID, STUDENT_ID);

        // then
        assertThat(score).isEqualTo(SCORE);
    }

    @Test
    @DisplayName("제출되지 않은 과제의 점수 조회")
    void getScore_NotSubmitted() {
        // given
        // 제출되지 않은 과제 멤버 생성
        WorkMemberEntity notSubmittedWorkMember = WorkMemberEntity.builder()
                .workMemberId(workMember.getWorkMemberId())
                .work(work)
                .user(student)
                .isSubmited(false)  // 제출 안됨
                .build();
                
        when(workMemberRepository.findByWorkIdAndUserId(WORK_ID, STUDENT_ID)).thenReturn(Optional.of(notSubmittedWorkMember));

        // when
        int score = feedbackService.getScore(WORK_ID, STUDENT_ID);

        // then
        assertThat(score).isEqualTo(0);
    }

    @Test
    @DisplayName("피드백이 없는 과제의 점수 조회")
    void getScore_NoFeedback() {
        // given
        when(workMemberRepository.findByWorkIdAndUserId(WORK_ID, STUDENT_ID)).thenReturn(Optional.of(workMember));
        when(workFeedbackRepository.findByWorkIdAndStudentId(WORK_ID, STUDENT_ID)).thenReturn(Optional.empty());

        // when
        int score = feedbackService.getScore(WORK_ID, STUDENT_ID);

        // then
        assertThat(score).isEqualTo(0);
    }
} 