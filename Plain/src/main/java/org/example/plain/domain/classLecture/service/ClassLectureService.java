package org.example.plain.domain.classLecture.service;

import lombok.RequiredArgsConstructor;
import org.example.plain.common.enums.Role;
import org.example.plain.domain.classLecture.dto.ClassAddRequest;
import org.example.plain.domain.classLecture.dto.ClassMemberResponse;
import org.example.plain.domain.classLecture.dto.ClassResponse;
import org.example.plain.domain.classLecture.entity.ClassLecture;
import org.example.plain.domain.classLecture.repository.ClassLectureRepositoryPort;
import org.example.plain.domain.classLecture.util.CodeGenerator;
import org.example.plain.domain.classMember.entity.ClassMember;
import org.example.plain.domain.user.entity.User;
import org.example.plain.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassLectureService {

    private final ClassLectureRepositoryPort classLectureRepositoryPort;
    private final UserRepository userRepository;

    /**
     * 클래스 생성
     * @param classAddRequest
     * @return
     */
    public ClassResponse createClass(ClassAddRequest classAddRequest, String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String code = generateCode();

        ClassLecture classLecture = classAddRequest.toEntity(user, code);
        classLectureRepositoryPort.save(classLecture);

        return ClassResponse.from(classLecture);
    }

    /**
     * 특정 클래스 조회
     * @param classId
     * @return
     */
    public ClassResponse getClass(String classId){
        ClassLecture classLecture = classLectureRepositoryPort.findById(classId);

        return ClassResponse.from(classLecture);
    }

    /**
     * 클래스 전체 조회
     * @return
     */
    public List<ClassResponse> getAllClass(){
        return classLectureRepositoryPort.findAll()
                .stream().map(ClassResponse::from).toList();
    }

    /**
     * 클래스 삭제
     * @param userId
     * @param classId
     */
    public ClassResponse deleteClass(String classId, String userId){
        ClassLecture classLecture = classLectureRepositoryPort.findById(classId);
        User user = userRepository.findById(userId).orElseThrow();

        if (classLecture.getInstructor().getId().equals(user.getId())) {
            throw new RuntimeException("삭제 할 수 없습니다");
        }

        classLectureRepositoryPort.delete(classId);
        return ClassResponse.builder()
                .id(classLecture.getId())
                .title(classLecture.getTitle())
                .description(classLecture.getDescription())
                .code(classLecture.getCode())
                .build();
    }

    /**
     * 클래스 수정
     * @param userId
     * @param classId
     * @param ClassAddRequest
     * @return
     */
    public ClassResponse modifiedClass(ClassAddRequest ClassAddRequest, String classId, String userId) {
        ClassLecture classLecture = classLectureRepositoryPort.findById(classId);
        User user = userRepository.findById(userId).orElseThrow();

        if (!classLecture.getInstructor().getId().equals(user.getId()) || !user.getRole().equals(Role.LEADER_CLASS)) {
            throw new RuntimeException("수정 할 수 없습니다");
        }

        classLecture.updateClass(ClassAddRequest);
        classLectureRepositoryPort.save(classLecture);

        return ClassResponse.from(classLecture);
    }

    /**
     * 클래스 멤버 조회
     * @param classId
     * @return
     */
    public List<ClassMemberResponse> getClassMembers(String classId) {
        ClassLecture classLecture = classLectureRepositoryPort.findById(classId);
        List<ClassMember> members = classLecture.getMembers();
        return ClassMemberResponse.from(members);
    }

    /**
     * 코드 생성기
     * @return
     */
    public String generateCode() {
        return CodeGenerator.generateCode();
    }

    /**
     * 클래스 초대코드 생성
     * @param
     * @return
     */
    public String createCode(String classId) {
        ClassLecture classLecture = classLectureRepositoryPort.findById(classId);

        return "https://www.example.com/invite?code=" + classLecture.getCode();
    }
}

