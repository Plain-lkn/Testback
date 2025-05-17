package org.example.plain.domain.lecture.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.plain.common.ResponseField;
import org.example.plain.common.enums.Message;
import org.example.plain.domain.lecture.dto.LectureResponse;
import org.example.plain.domain.lecture.entity.LectureEntity;
import org.example.plain.domain.lecture.entity.member.LectureMemberEntity;
import org.example.plain.domain.lecture.repository.LectureMemberRepository;
import org.example.plain.domain.lecture.repository.LectureRepository;
import org.example.plain.domain.lecture.service.interfaces.LectureMemberService;
import org.example.plain.domain.user.dto.UserResponse;
import org.example.plain.domain.user.entity.User;
import org.example.plain.domain.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LectureMemberServiceImpl implements LectureMemberService {

    private final LectureRepository lectureRepository;
    private final LectureMemberRepository lectureMemberRepository;
    private final UserRepository userRepository;

    @Override
    public ResponseField<List<UserResponse>> getStudentList(String lectureId) {
        LectureEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));

        List<UserResponse> students = lectureMemberRepository.findByLectureId(lectureId)
                .stream()
                .map(LectureMemberEntity::getUser)
                .map(UserResponse::chaingeUsertoUserResponse)
                .collect(Collectors.toList());

        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, students);
    }

    @Override
    public ResponseField<List<LectureResponse>> getLectureMyList(String userId) {
        List<LectureResponse> lectures = lectureMemberRepository.findByUserId(userId)
                .stream()
                .map(LectureMemberEntity::getLecture)
                .map(LectureResponse::from)
                .collect(Collectors.toList());

        if (lectures.isEmpty()) {
            throw new NoSuchElementException("수강 중인 강의가 없습니다.");
        }

        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, lectures);
    }

    @Override
    @Transactional
    public ResponseField<Void> enrollLecture(String lectureId, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        LectureEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));

        if (lectureMemberRepository.existsByLectureIdAndUserId(lectureId, userId)) {
            throw new IllegalArgumentException("이미 수강 중인 강의입니다.");
        }

        LectureMemberEntity lectureMember = LectureMemberEntity.create(user, lecture);
        lectureMemberRepository.save(lectureMember);
        
        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, null);
    }

    @Override
    @Transactional
    public ResponseField<Void> cancelEnrollment(String lectureId, String userId) {
        LectureMemberEntity lectureMember = lectureMemberRepository.findByLectureIdAndUserId(lectureId, userId)
                .orElseThrow(() -> new IllegalArgumentException("수강 중인 강의가 아닙니다."));

        lectureMemberRepository.delete(lectureMember);
        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, null);
    }
} 