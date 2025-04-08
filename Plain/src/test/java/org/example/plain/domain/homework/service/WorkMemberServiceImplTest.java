package org.example.plain.domain.homework.service;

import org.example.plain.common.config.SecurityUtils;
import org.example.plain.domain.classMember.entity.ClassMember;
import org.example.plain.domain.classMember.entity.ClassMemberId;
import org.example.plain.domain.classMember.repository.ClassMemberRepository;
import org.example.plain.domain.classLecture.entity.ClassLecture;
import org.example.plain.domain.homework.entity.WorkEntity;
import org.example.plain.domain.homework.entity.WorkMemberEntity;
import org.example.plain.domain.homework.entity.WorkMemberId;
import org.example.plain.domain.user.entity.User;
import org.example.plain.repository.BoardRepository;
import org.example.plain.repository.WorkMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkMemberServiceImplTest {

    @Mock
    private WorkMemberRepository workMemberRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private ClassMemberRepository classMemberRepository;

    private WorkMemberServiceImpl workMemberService;

    private User testUser;
    private User testInstructor;
    private WorkEntity testWork;
    private ClassMember testClassMember;
    private ClassLecture testClassLecture;

    @BeforeEach
    void setUp() {
        workMemberService = new WorkMemberServiceImpl(workMemberRepository, boardRepository, classMemberRepository);

        testUser = User.builder()
                .id("testUser")
                .username("Test User")
                .email("test@example.com")
                .build();

        testInstructor = User.builder()
                .id("testInstructor")
                .username("Test Instructor")
                .email("instructor@example.com")
                .build();
        
        testWork = WorkEntity.builder()
                .classId("testClassId")
                .title("Test Work")
                .content("Test Content")
                .type("WORK")
                .userId(testInstructor.getId())
                .workId("testWorkId")
                .deadline(LocalDateTime.now().plusDays(1))
                .build();

        testClassLecture = ClassLecture.builder()
                .instructor(testInstructor)
                .build();
        
        testClassMember = new ClassMember();
        testClassMember.setUser(testInstructor);
        testClassMember.setClassLecture(testClassLecture);
    }

    @Test
    void addHomeworkMember_Success() {
        // given
        when(boardRepository.findByWorkId("testWorkId"))
                .thenReturn(Optional.of(testWork));
        when(classMemberRepository.findById(any(ClassMemberId.class)))
                .thenReturn(Optional.of(testClassMember));
        when(workMemberRepository.existsById(any(WorkMemberId.class)))
                .thenReturn(false);

        // when
        workMemberService.addHomeworkMember("testWorkId", "testUser", testInstructor.getId());

        // then
        verify(workMemberRepository).save(any(WorkMemberEntity.class));
    }

    @Test
    void addHomeworkMember_AlreadyAssigned_ThrowsException() {
        // given
        when(boardRepository.findByWorkId("testWorkId"))
                .thenReturn(Optional.of(testWork));
        when(classMemberRepository.findById(any(ClassMemberId.class)))
                .thenReturn(Optional.of(testClassMember));
        when(workMemberRepository.existsById(any(WorkMemberId.class)))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> workMemberService.addHomeworkMember("testWorkId", "testUser", testInstructor.getId()))
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("이미 과제가 할당된 사용자입니다.");
    }

    @Test
    void addHomeworkMember_Unauthorized_ThrowsException() {
        // given
        when(boardRepository.findByWorkId("testWorkId"))
                .thenReturn(Optional.of(testWork));
        when(classMemberRepository.findById(any(ClassMemberId.class)))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> workMemberService.addHomeworkMember("testWorkId", "testUser", "unauthorizedUser"))
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("수업 멤버를 찾을 수 없습니다.");
    }

    @Test
    void addHomeworkMember_NotClassMember_ThrowsException() {
        // given
        when(boardRepository.findByWorkId("testWorkId"))
                .thenReturn(Optional.of(testWork));
        when(classMemberRepository.findById(any(ClassMemberId.class)))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> workMemberService.addHomeworkMember("testWorkId", "testUser", testInstructor.getId()))
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("클래스 멤버가 아닙니다.");
    }

    @Test
    void removeHomeworkMember_Success() {
        // given
        WorkMemberEntity workMemberEntity = new WorkMemberEntity();
        when(boardRepository.findByWorkId("testWorkId"))
                .thenReturn(Optional.of(testWork));
        when(classMemberRepository.findById(any(ClassMemberId.class)))
                .thenReturn(Optional.of(testClassMember));
        when(workMemberRepository.findById(any(WorkMemberId.class)))
                .thenReturn(Optional.of(workMemberEntity));

        // when
        workMemberService.removeHomeworkMember("testWorkId", "testUser", testInstructor.getId());

        // then
        verify(workMemberRepository).delete(workMemberEntity);
    }

    @Test
    void removeHomeworkMember_NotFound_ThrowsException() {
        // given
        when(boardRepository.findByWorkId("testWorkId"))
                .thenReturn(Optional.of(testWork));
        when(classMemberRepository.findById(any(ClassMemberId.class)))
                .thenReturn(Optional.of(testClassMember));
        when(workMemberRepository.findById(any(WorkMemberId.class)))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> workMemberService.removeHomeworkMember("testWorkId", "testUser", testInstructor.getId()))
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("과제 멤버를 찾을 수 없습니다.");
    }

    @Test
    void getSingleMembers_Success() {
        // given
        WorkMemberEntity workMemberEntity = new WorkMemberEntity();
        when(workMemberRepository.findById(any(WorkMemberId.class)))
                .thenReturn(Optional.of(workMemberEntity));

        // when
        var result = workMemberService.getSingleMembers("testWorkId", "testUser");

        // then
        assertThat(result).isNotNull();
    }

    @Test
    void getSingleMembers_NotFound_ThrowsException() {
        // given
        when(workMemberRepository.findById(any(WorkMemberId.class)))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> workMemberService.getSingleMembers("testWorkId", "testUser"))
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("과제 멤버를 찾을 수 없습니다.");
    }
} 