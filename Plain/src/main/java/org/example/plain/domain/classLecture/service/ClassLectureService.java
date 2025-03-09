package org.example.plain.domain.classLecture.service;

import lombok.RequiredArgsConstructor;
import org.example.plain.domain.classLecture.dto.ClassAddRequest;
import org.example.plain.domain.classLecture.dto.ClassRequest;
import org.example.plain.domain.classLecture.dto.ClassResponse;
import org.example.plain.domain.classLecture.entity.ClassLecture;
import org.example.plain.domain.classLecture.repository.ClassLectureRepositoryPort;
import org.example.plain.domain.classLecture.util.CodeGenerator;
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
     * @param classRequest
     * @return
     */
    public ClassResponse modifiedClass(ClassRequest classRequest, String classId, String userId) {
        ClassLecture classLecture = classLectureRepositoryPort.findById(classId);
        User user = userRepository.findById(userId).orElseThrow();

        if (classLecture.getInstructor().getId().equals(user.getId())) {
            throw new RuntimeException("수정 할 수 없습니다");
        }
        String title = classRequest.title();
        String description = classRequest.description();

        classLecture.updateClass(title, description);
        classLectureRepositoryPort.save(classLecture);

        return ClassResponse.from(classLecture);
    }

//    public List<User> getClassMembers(Long classId) {
//        ClassLecture classLecture = classLectureRepositoryPort.findById(classId);
//        return classLecture.getUser()
//    }

    /**
     * 코드 생성기
     * @return
     */
    public String generateCode() {
        return CodeGenerator.generateCode();
    }
}

