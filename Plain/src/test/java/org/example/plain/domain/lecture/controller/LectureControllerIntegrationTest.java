package org.example.plain.domain.lecture.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.plain.common.config.SecurityUtils;
import org.example.plain.common.enums.Message;
import org.example.plain.common.enums.Role;
import org.example.plain.domain.lecture.dto.LectureRequest;
import org.example.plain.domain.lecture.entity.LectureEntity;
import org.example.plain.domain.lecture.entity.member.LectureMemberEntity;
import org.example.plain.domain.lecture.enums.LectureType;
import org.example.plain.domain.lecture.repository.LectureMemberRepository;
import org.example.plain.domain.lecture.repository.LectureRepository;
import org.example.plain.domain.user.entity.User;
import org.example.plain.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "testuser", roles = {"USER", "TEACHER"})
public class LectureControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private LectureMemberRepository lectureMemberRepository;

    private User instructor;
    private User student;
    private LectureEntity lecture;

    // 짧은 ID 생성 메서드
    private String generateShortId() {
        return "test" + Math.abs(UUID.randomUUID().toString().hashCode() % 10000);
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        // Create test users with shorter IDs
        instructor = User.builder()
                .id(generateShortId())
                .username("Test Instructor")
                .email("instructor@test.com")
                .role(Role.TEACHER)
                .build();
        userRepository.save(instructor);

        student = User.builder()
                .id(generateShortId())
                .username("Test Student")
                .email("student@test.com")
                .role(Role.NORMAL)
                .build();
        userRepository.save(student);

        // Create test lecture with shorter ID
        lecture = LectureEntity.builder()
                .id(generateShortId())
                .user(instructor)
                .lectureType(LectureType.VIDEO)
                .lectureName("Integration Test Lecture")
                .lectureDescription("Test Description for Integration Test")
                .lecturePrice(15000)
                .build();
        lectureRepository.save(lecture);
    }

    @Test
    @DisplayName("UC-1: 강의 목록 조회")
    void getLectureList() throws Exception {
        // When
        ResultActions result = mockMvc.perform(get("/lectures/shop/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is(Message.OK.name())))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].lectureId", is(notNullValue())))
                .andExpect(jsonPath("$.data[0].lectureName", is(notNullValue())));
    }

    @Test
    @DisplayName("UC-2: 강의 상세 정보 조회")
    void getLectureDetail() throws Exception {
        // When
        ResultActions result = mockMvc.perform(get("/lectures/{lecture_id}", lecture.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is(Message.OK.name())))
                .andExpect(jsonPath("$.data.lectureId", is(lecture.getId())))
                .andExpect(jsonPath("$.data.lectureName", is(lecture.getLectureName())))
                .andExpect(jsonPath("$.data.lectureDescription", is(lecture.getLectureDescription())))
                .andExpect(jsonPath("$.data.instructorName", is(instructor.getUsername())));
    }

    @Test
    @DisplayName("UC-3: 강의 생성 및 수정")
    void createAndUpdateLecture() throws Exception {
        // Given - Create lecture request
        LectureRequest createRequest = new LectureRequest(
                LectureType.LIVE,
                "New Test Lecture",
                "New Description",
                20000
        );

        try (MockedStatic<SecurityUtils> mockedStatic = mockStatic(SecurityUtils.class)) {
            // Mock SecurityUtils to return instructor ID
            mockedStatic.when(SecurityUtils::getUserId).thenReturn(instructor.getId());

            // Step 1: Create lecture
            ResultActions createResult = mockMvc.perform(post("/lectures")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andDo(print());

            createResult.andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", is(Message.OK.name())))
                    .andExpect(jsonPath("$.data.lectureName", is(createRequest.getLectureName())));

            String newLectureId = objectMapper.readTree(createResult.andReturn().getResponse().getContentAsString())
                    .path("data").path("lectureId").asText();

            // Step 2: Update lecture
            LectureRequest updateRequest = new LectureRequest(
                    LectureType.VIDEO,
                    "Updated Test Lecture",
                    "Updated Description",
                    25000
            );

            ResultActions updateResult = mockMvc.perform(put("/lectures/{lecture_id}", newLectureId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print());

            updateResult.andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", is(Message.OK.name())))
                    .andExpect(jsonPath("$.data.lectureName", is(updateRequest.getLectureName())));

            // Verify the lecture was updated in the repository
            LectureEntity updatedLecture = lectureRepository.findById(newLectureId).orElseThrow();
            assertEquals(updateRequest.getLectureName(), updatedLecture.getLectureName());
            assertEquals(updateRequest.getLectureDescription(), updatedLecture.getLectureDescription());
            assertEquals(updateRequest.getLecturePrice(), updatedLecture.getLecturePrice());
        }
    }

    @Test
    @DisplayName("UC-4: 강의 수강 신청 및 취소")
    void enrollAndCancelLecture() throws Exception {
        try (MockedStatic<SecurityUtils> mockedStatic = mockStatic(SecurityUtils.class)) {
            // Mock SecurityUtils to return student ID
            mockedStatic.when(SecurityUtils::getUserId).thenReturn(student.getId());

            // Step 1: Enroll in the lecture
            ResultActions enrollResult = mockMvc.perform(post("/lectures/{lecture_id}/enroll", lecture.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print());

            enrollResult.andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", is(Message.OK.name())));

            // Verify enrollment in the repository
            assertTrue(lectureMemberRepository.existsByLectureIdAndUserId(lecture.getId(), student.getId()));

            // Step 2: Get enrolled lectures
            ResultActions myLecturesResult = mockMvc.perform(get("/lectures/my")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print());

            myLecturesResult.andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", is(Message.OK.name())))
                    .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$.data[0].lectureId", is(lecture.getId())));

            // Step 3: Cancel enrollment
            ResultActions cancelResult = mockMvc.perform(delete("/lectures/{lecture_id}/enroll", lecture.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print());

            cancelResult.andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", is(Message.OK.name())));

            // Verify enrollment was canceled
            assertEquals(false, lectureMemberRepository.existsByLectureIdAndUserId(lecture.getId(), student.getId()));
        }
    }

    @Test
    @DisplayName("UC-5: 강사는 강의 수강생 목록을 조회할 수 있다")
    void instructorCanViewStudentList() throws Exception {
        // Given: Student enrolls in the lecture
        LectureMemberEntity enrollment = LectureMemberEntity.builder()
                .user(student)
                .lecture(lecture)
                .build();
        lectureMemberRepository.save(enrollment);

        try (MockedStatic<SecurityUtils> mockedStatic = mockStatic(SecurityUtils.class)) {
            // Mock SecurityUtils to return instructor ID
            mockedStatic.when(SecurityUtils::getUserId).thenReturn(instructor.getId());

            // When: Instructor requests the student list
            ResultActions result = mockMvc.perform(get("/lectures/{lecture_id}/students", lecture.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print());

            // Then: The student list is returned
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", is(Message.OK.name())))
                    .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$.data[0].userId", is(student.getId())))
                    .andExpect(jsonPath("$.data[0].name", is(student.getUsername())));
        }
    }

    @Test
    @DisplayName("UC-6: 강의 삭제")
    void deleteLecture() throws Exception {
        try (MockedStatic<SecurityUtils> mockedStatic = mockStatic(SecurityUtils.class)) {
            // Mock SecurityUtils to return instructor ID
            mockedStatic.when(SecurityUtils::getUserId).thenReturn(instructor.getId());

            // When: Instructor deletes the lecture
            ResultActions result = mockMvc.perform(delete("/lectures/{lecture_id}", lecture.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print());

            // Then: The lecture is deleted
            result.andExpect(status().isOk());

            // Verify the lecture was deleted
            assertEquals(false, lectureRepository.existsById(lecture.getId()));
        }
    }
} 