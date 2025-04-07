package org.example.plain.domain.homework.integration;

import org.example.plain.common.enums.Role;
import org.example.plain.domain.homework.dto.Work;
import org.example.plain.domain.homework.dto.WorkMember;
import org.example.plain.domain.homework.dto.WorkSubmitField;
import org.example.plain.domain.homework.dto.WorkSubmitListResponse;
import org.example.plain.domain.homework.interfaces.SubmissionService;
import org.example.plain.domain.homework.interfaces.WorkMemberService;
import org.example.plain.domain.homework.interfaces.WorkService;
import org.example.plain.domain.user.entity.User;
import org.example.plain.domain.user.repository.UserRepository;
import org.example.plain.domain.classLecture.entity.ClassLecture;
import org.example.plain.domain.classMember.entity.ClassMember;
import org.example.plain.domain.classMember.entity.ClassMemberId;
import org.example.plain.domain.classMember.repository.ClassMemberRepository;
import org.example.plain.domain.classLecture.repository.ClassLectureRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.event.annotation.AfterTestClass;
import org.springframework.test.context.event.annotation.AfterTestMethod;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.test.context.event.annotation.BeforeTestMethod;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class HomeworkIntegrationTest {
    private static final String TEST_WORK_ID = "test-work-1";
    private static final String TEST_INSTRUCTOR_ID = "instructor1";
    private static final String TEST_STUDENT_ID = "student1";
    private static final String TEST_CLASS_ID = "class1";
    private static final String TEST_BOARD_ID = "board1";
    private static final String TEST_TITLE = "Test Work";
    private static final String TEST_CONTENT = "Test Description";

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private WorkService workService;

    @Autowired
    private WorkMemberService workMemberService;

    @Autowired
    private ClassLectureRepository classLectureRepository;

    @Autowired
    private ClassMemberRepository classMemberRepository;

    @Autowired
    private UserRepository userRepository;

    private Work testWork;
    private User testInstructor;
    private User testStudent;
    private MultipartFile testFile;
    private ClassLecture testClass;
    private ClassMember instructorClassMember;
    private ClassMember studentClassMember;

    @BeforeEach
    void setUp() {
        // 테스트용 강사(클래스 생성자) 생성
        testInstructor = User.builder()
                .id(TEST_INSTRUCTOR_ID)
                .username("Test Instructor")
                .email("instructor@example.com")
                .role(Role.TEACHER)
                .build();
        userRepository.save(testInstructor);

        // 테스트용 학생(클래스 멤버) 생성
        testStudent = User.builder()
                .id(TEST_STUDENT_ID)
                .username("Test Student")
                .email("student@example.com")
                .role(Role.NORMAL)
                .build();
        userRepository.save(testStudent);

        // 테스트용 클래스 생성
        testClass = ClassLecture.builder()
                .id(TEST_CLASS_ID)
                .title("Test Class")
                .description("Test Description")
                .code("TEST123")
                .instructor(testInstructor)
                .build();
        testClass = classLectureRepository.save(testClass);

        // 강사를 클래스 멤버로 등록
        ClassMemberId instructorMemberId = new ClassMemberId(testClass.getId(), testInstructor.getId());
        instructorClassMember = ClassMember.builder()
                .id(instructorMemberId)
                .classLecture(testClass)
                .user(testInstructor)
                .build();
        instructorClassMember = classMemberRepository.save(instructorClassMember);

        // 학생을 클래스 멤버로 등록
        ClassMemberId studentMemberId = new ClassMemberId(testClass.getId(), testStudent.getId());
        studentClassMember = ClassMember.builder()
                .id(studentMemberId)
                .classLecture(testClass)
                .user(testStudent)
                .build();
        studentClassMember = classMemberRepository.save(studentClassMember);

        // 테스트용 과제 생성
        testWork = Work.builder()
                .boardId(TEST_BOARD_ID)
                .groupId(TEST_CLASS_ID)
                .writer(TEST_INSTRUCTOR_ID)
                .workId(TEST_WORK_ID)
                .title(TEST_TITLE)
                .content(TEST_CONTENT)
                .deadline(LocalDateTime.now().plusDays(7))
                .build();

        // 테스트용 파일 생성
        testFile = new MockMultipartFile(
                "test.txt",
                "test.txt",
                "text/plain",
                "Test content".getBytes()
        );

        // 과제 저장
        workService.insertWork(testWork, testClass.getId(), testInstructor.getId());
        // 저장된 과제의 workId를 가져옴
        testWork = workService.selectWork(TEST_WORK_ID);
    }

    @AfterEach
    void tearDown() {
        // 테스트 데이터 정리
        try {
            // 과제 제출 취소
            try {
                if (submissionService.isSubmitted(TEST_WORK_ID, TEST_STUDENT_ID)) {
                    submissionService.cancelSubmission(TEST_WORK_ID, TEST_STUDENT_ID);
                }
            } catch (Exception e) {
                // 제출 정보가 없는 경우 무시
            }

            // 과제 멤버 제거
            try {
                workMemberService.removeHomeworkMember(TEST_WORK_ID, TEST_STUDENT_ID, TEST_INSTRUCTOR_ID);
            } catch (Exception e) {
                // 멤버가 없는 경우 무시
            }

            // 과제 삭제
            try {
                workService.deleteWork(TEST_WORK_ID);
            } catch (Exception e) {
                // 과제가 없는 경우 무시
            }

            // 추가 과제 삭제
            try {
                workService.deleteWork("test-work-2");
            } catch (Exception e) {
                // 과제가 없는 경우 무시
            }

            // 클래스 멤버 삭제
            try {
                classMemberRepository.delete(instructorClassMember);
                classMemberRepository.delete(studentClassMember);
            } catch (Exception e) {
                // 멤버가 없는 경우 무시
            }

            // 클래스 삭제
            try {
                classLectureRepository.delete(testClass);
            } catch (Exception e) {
                // 클래스가 없는 경우 무시
            }

            // 사용자 삭제
            try {
                userRepository.delete(testInstructor);
                userRepository.delete(testStudent);
            } catch (Exception e) {
                // 사용자가 없는 경우 무시
            }
        } catch (Exception e) {
            // 정리 과정에서 발생한 예외는 로깅만 하고 계속 진행
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }

    @Nested
    @DisplayName("과제 제출 통합 테스트")
    class SubmissionIntegration {
        @Test
        @DisplayName("과제 제출 성공")
        void submitWork_Success() {
            // given
            workMemberService.addHomeworkMember(TEST_WORK_ID, TEST_STUDENT_ID, TEST_INSTRUCTOR_ID);
            WorkSubmitField submitField = WorkSubmitField.builder()
                    .workId(TEST_WORK_ID)
                    .userId(TEST_STUDENT_ID)
                    .file(Arrays.asList(testFile))
                    .build();

            // when
            submissionService.submit(submitField);

            // then
            List<String> submissions = submissionService.getSubmissionFiles(TEST_WORK_ID, TEST_STUDENT_ID);
            assertThat(submissions).isNotEmpty();
        }

        @Test
        @DisplayName("과제 제출 실패 - 과제 멤버가 아닌 경우")
        void submitWork_Fail_NotWorkMember() {
            // given
            WorkSubmitField submitField = WorkSubmitField.builder()
                    .workId(TEST_WORK_ID)
                    .userId(TEST_STUDENT_ID)
                    .file(Arrays.asList(testFile))
                    .build();

            // when & then
            assertThrows(HttpClientErrorException.class, () ->
                    submissionService.submit(submitField),
                    "과제 할당 정보를 찾을 수 없습니다."
            );
        }

        @Test
        @DisplayName("과제 제출 목록 조회")
        void getSubmissionList_Success() {
            // given
            workMemberService.addHomeworkMember(TEST_WORK_ID, TEST_STUDENT_ID, TEST_INSTRUCTOR_ID);
            WorkSubmitField submitField = WorkSubmitField.builder()
                    .workId(TEST_WORK_ID)
                    .userId(TEST_STUDENT_ID)
                    .file(Arrays.asList(testFile))
                    .build();
            submissionService.submit(submitField);

            // when
            List<WorkSubmitListResponse> submissions = submissionService.getSubmissionList(TEST_WORK_ID);

            // then
            assertThat(submissions).hasSize(1);
            assertThat(submissions.get(0).getUserId()).isEqualTo(TEST_STUDENT_ID);
        }

        @Test
        @DisplayName("과제 제출 상태 확인")
        void isSubmitted_Success() {
            // given
            workMemberService.addHomeworkMember(TEST_WORK_ID, TEST_STUDENT_ID, TEST_INSTRUCTOR_ID);
            WorkSubmitField submitField = WorkSubmitField.builder()
                    .workId(TEST_WORK_ID)
                    .userId(TEST_STUDENT_ID)
                    .file(Arrays.asList(testFile))
                    .build();
            submissionService.submit(submitField);

            // when
            boolean isSubmitted = submissionService.isSubmitted(TEST_WORK_ID, TEST_STUDENT_ID);

            // then
            assertThat(isSubmitted).isTrue();
        }

        @Test
        @DisplayName("과제 제출 취소")
        void cancelSubmission_Success() {
            // given
            workMemberService.addHomeworkMember(TEST_WORK_ID, TEST_STUDENT_ID, TEST_INSTRUCTOR_ID);
            WorkSubmitField submitField = WorkSubmitField.builder()
                    .workId(TEST_WORK_ID)
                    .userId(TEST_STUDENT_ID)
                    .file(Arrays.asList(testFile))
                    .build();
            submissionService.submit(submitField);

            // when
            submissionService.cancelSubmission(TEST_WORK_ID, TEST_STUDENT_ID);

            // then
            boolean isSubmitted = submissionService.isSubmitted(TEST_WORK_ID, TEST_STUDENT_ID);
            assertThat(isSubmitted).isFalse();
        }
    }

    @Nested
    @DisplayName("과제 멤버 통합 테스트")
    class WorkMemberIntegration {
        @Test
        @DisplayName("과제 멤버 추가 성공")
        void addHomeworkMember_Success() {
            // when
            workMemberService.addHomeworkMember(TEST_WORK_ID, TEST_STUDENT_ID, TEST_INSTRUCTOR_ID);

            // then
            List<WorkMember> members = workMemberService.homeworkMembers(TEST_WORK_ID);
            assertThat(members).hasSize(1);
            assertThat(members.get(0).getUser().getId()).isEqualTo(TEST_STUDENT_ID);
        }

        @Test
        @DisplayName("과제 멤버 제거 성공")
        void removeHomeworkMember_Success() {
            // given
            workMemberService.addHomeworkMember(TEST_WORK_ID, TEST_STUDENT_ID, TEST_INSTRUCTOR_ID);

            // when
            workMemberService.removeHomeworkMember(TEST_WORK_ID, TEST_STUDENT_ID, TEST_INSTRUCTOR_ID);

            // then
            List<WorkMember> members = workMemberService.homeworkMembers(TEST_WORK_ID);
            assertThat(members).isEmpty();
        }

        @Test
        @DisplayName("과제 멤버 목록 조회")
        void homeworkMembers_Success() {
            // given
            workMemberService.addHomeworkMember(TEST_WORK_ID, TEST_STUDENT_ID, TEST_INSTRUCTOR_ID);
            workMemberService.addHomeworkMember(TEST_WORK_ID, "user2", TEST_INSTRUCTOR_ID);

            // when
            List<WorkMember> members = workMemberService.homeworkMembers(TEST_WORK_ID);

            // then
            assertThat(members).hasSize(2);
            assertThat(members).extracting(member -> member.getUser().getId())
                    .contains(TEST_STUDENT_ID, "user2");
        }

        @Test
        @DisplayName("단일 과제 멤버 조회")
        void getSingleMembers_Success() {
            // given
            workMemberService.addHomeworkMember(TEST_WORK_ID, TEST_STUDENT_ID, TEST_INSTRUCTOR_ID);

            // when
            WorkMember member = workMemberService.getSingleMembers(TEST_WORK_ID, TEST_STUDENT_ID);

            // then
            assertThat(member).isNotNull();
            assertThat(member.getUser().getId()).isEqualTo(TEST_STUDENT_ID);
        }
    }

    @Nested
    @DisplayName("과제 통합 테스트")
    class WorkIntegration {
        @Test
        @DisplayName("과제 생성 성공")
        void insertWork_Success() {
            // given
            Work newWork = Work.builder()
                    .boardId("board2")
                    .groupId(TEST_CLASS_ID)
                    .writer(TEST_INSTRUCTOR_ID)
                    .workId("test-work-2")
                    .title("Another Work")
                    .content("Another Description")
                    .deadline(LocalDateTime.now().plusDays(7))
                    .build();

            // when
            workService.insertWork(newWork, TEST_CLASS_ID, TEST_INSTRUCTOR_ID);

            // then
            Work savedWork = workService.selectWork("test-work-2");
            assertThat(savedWork).isNotNull();
            assertThat(savedWork.getTitle()).isEqualTo("Another Work");
        }

        @Test
        @DisplayName("과제 수정 성공")
        void updateWork_Success() {
            // given
            Work updatedWork = Work.builder()
                    .boardId(TEST_BOARD_ID)
                    .groupId(TEST_CLASS_ID)
                    .writer(TEST_INSTRUCTOR_ID)
                    .workId(TEST_WORK_ID)
                    .title("Updated Title")
                    .content("Updated Description")
                    .deadline(LocalDateTime.now().plusDays(14))
                    .build();

            // when
            workService.updateWork(updatedWork, TEST_WORK_ID, TEST_INSTRUCTOR_ID);

            // then
            Work savedWork = workService.selectWork(TEST_WORK_ID);
            assertThat(savedWork.getTitle()).isEqualTo("Updated Title");
            assertThat(savedWork.getContent()).isEqualTo("Updated Description");
        }

        @Test
        @DisplayName("과제 삭제 성공")
        void deleteWork_Success() {
            // when
            workService.deleteWork(TEST_WORK_ID);

            // then
            Work deletedWork = workService.selectWork(TEST_WORK_ID);
            assertThat(deletedWork).isNull();
        }

        @Test
        @DisplayName("그룹 과제 목록 조회")
        void selectGroupWorks_Success() {
            // given
            Work anotherWork = Work.builder()
                    .boardId("board2")
                    .groupId(TEST_CLASS_ID)
                    .writer(TEST_INSTRUCTOR_ID)
                    .workId("test-work-2")
                    .title("Another Work")
                    .content("Another Description")
                    .deadline(LocalDateTime.now().plusDays(7))
                    .build();
            workService.insertWork(anotherWork, TEST_CLASS_ID, TEST_INSTRUCTOR_ID);

            // when
            List<Work> works = workService.selectGroupWorks(TEST_CLASS_ID);

            // then
            assertThat(works).hasSize(2);
            assertThat(works).extracting(Work::getTitle)
                    .contains(TEST_TITLE, "Another Work");
        }
    }

    @Nested
    @DisplayName("테스트 데이터 정리")
    class CleanupTest {
        @Test
        @DisplayName("테스트 데이터가 정상적으로 정리되는지 확인")
        void verifyCleanup() {
            // given
            workMemberService.addHomeworkMember(TEST_WORK_ID, TEST_STUDENT_ID, TEST_INSTRUCTOR_ID);
            WorkSubmitField submitField = WorkSubmitField.builder()
                    .workId(TEST_WORK_ID)
                    .userId(TEST_STUDENT_ID)
                    .file(Arrays.asList(testFile))
                    .build();
            submissionService.submit(submitField);

            // when
            // @AfterEach에서 자동으로 정리됨

            // then
            // 다음 테스트에서 검증됨
        }

        @Test
        @DisplayName("정리 후 데이터가 존재하지 않는지 확인")
        void verifyNoDataAfterCleanup() {
            // given
            // 이전 테스트의 데이터가 정리된 상태

            // when & then
            assertThat(workMemberService.homeworkMembers(TEST_WORK_ID)).isEmpty();
            assertThat(submissionService.isSubmitted(TEST_WORK_ID, TEST_STUDENT_ID)).isFalse();
            assertThat(workService.selectWork(TEST_WORK_ID)).isNull();
        }
    }

    @Nested
    @DisplayName("클래스 멤버 통합 테스트")
    class ClassMemberIntegration {
        @Test
        @DisplayName("클래스 멤버가 아닌 경우 과제 생성 실패")
        void insertWork_Fail_NotClassMember() {
            // given
            Work work = Work.builder()
                    .boardId(TEST_BOARD_ID)
                    .groupId("non-existent-class")
                    .writer(TEST_INSTRUCTOR_ID)
                    .workId("test-work-2")
                    .title(TEST_TITLE)
                    .content(TEST_CONTENT)
                    .deadline(LocalDateTime.now().plusDays(7))
                    .build();

            // when & then
            assertThrows(HttpClientErrorException.class, () ->
                    workService.insertWork(work, "non-existent-class", TEST_INSTRUCTOR_ID),
                    "클래스 멤버가 아닙니다."
            );
        }

        @Test
        @DisplayName("클래스 멤버가 아닌 경우 과제 멤버 추가 실패")
        void addHomeworkMember_Fail_NotClassMember() {
            // when & then
            assertThrows(HttpClientErrorException.class, () ->
                    workMemberService.addHomeworkMember(TEST_WORK_ID, "non-existent-user", TEST_INSTRUCTOR_ID),
                    "클래스 멤버가 아닙니다."
            );
        }

        @Test
        @DisplayName("클래스 멤버가 아닌 경우 과제 제출 실패")
        void submitWork_Fail_NotClassMember() {
            // given
            WorkSubmitField submitField = WorkSubmitField.builder()
                    .workId(TEST_WORK_ID)
                    .userId("non-existent-user")
                    .file(Arrays.asList(testFile))
                    .build();

            // when & then
            assertThrows(HttpClientErrorException.class, () ->
                    submissionService.submit(submitField),
                    "클래스 멤버가 아닙니다."
            );
        }
    }
} 