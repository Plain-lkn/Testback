package org.example.plain.domain.notice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.plain.common.ResponseField;
import org.example.plain.common.enums.Message;
import org.example.plain.domain.classLecture.entity.ClassLecture;
import org.example.plain.domain.classLecture.repository.ClassLectureRepositoryPort;
import org.example.plain.domain.notice.dto.*;
import org.example.plain.domain.notice.entity.NoticeCommentEntity;
import org.example.plain.domain.notice.entity.NoticeEntity;
import org.example.plain.domain.notice.repository.NoticeCommentRepository;
import org.example.plain.domain.notice.repository.NoticeRepository;
import org.example.plain.domain.user.entity.User;
import org.example.plain.domain.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeCommentRepository noticeCommentRepository;
    private final UserRepository userRepository;
    private final ClassLectureRepositoryPort classLectureRepositoryPort;

    @Transactional
    public ResponseField<NoticeResponse> createNotice(NoticeRequest noticeRequest, String userId) {
        // User ID로 User 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        ClassLecture classLecture = classLectureRepositoryPort.findById(noticeRequest.getC_id());


        // NoticeEntity 생성 (User 포함)
        NoticeEntity createNotice = NoticeEntity.create(
                noticeRequest.getTitle(),
                noticeRequest.getContent(),
                user,
                classLecture

        );

        // 저장
        NoticeEntity noticeEntity = noticeRepository.save(createNotice);

        // NoticeResponse 생성 및 반환
        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, NoticeResponse.from(noticeEntity));
    }


    /**
     * 공지사항 수정
     * @param noticeUpdateRequest
     * @return
     */
    @Transactional
    public ResponseField<NoticeResponse> updateNotice(NoticeUpdateRequest noticeUpdateRequest, String userId) {

        NoticeEntity noticeEntity = noticeRepository.findById(noticeUpdateRequest.getNoticeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지사항입니다."));

        if (!noticeEntity.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }
                noticeEntity.update(
                noticeUpdateRequest.getNoticeId(),
                noticeUpdateRequest.getTitle(),
                noticeUpdateRequest.getContent()
                );

        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, NoticeResponse.from(noticeEntity));
    }

    @Transactional(readOnly = true)
    public ResponseField<List<NoticeResponse>> getAllNotice(){
        // findAll 대신 findAllWithUserAndClassLecture 메서드를 사용하여 Fetch Join 사용
        List<NoticeEntity> allNotice = noticeRepository.findAllWithUserAndClassLecture();
        log.info("lists",allNotice);
        List<NoticeResponse> noticeResponses = allNotice.stream()
                .map(NoticeResponse::from) // 메서드 참조로 간결하게 변경
                .toList();

        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, noticeResponses);
    }

    @Transactional(readOnly = true)
    public ResponseField<NoticeResponse> getNotice(String noticeId){
       return new ResponseField<>(Message.OK.name(), HttpStatus.OK, NoticeResponse.from(noticeRepository.findById(noticeId).orElseThrow()));
    }

    /**
     * 클래스 삭제
     * @param noticeId
     */
    @Transactional
    public void deleteNotice(String noticeId, String userId) {
        NoticeEntity noticeEntity = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!noticeEntity.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        noticeRepository.delete(noticeEntity);
    }

    @Transactional
    public ResponseField<NoticeCommentResponse> createNoticeComments(NoticeCommentRequest noticeCommentRequest, String userId ) {
        // User ID로 User 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // NoticeEntity 생성 (User 포함)
        NoticeCommentEntity createNoticeComments = NoticeCommentEntity.create(
                noticeCommentRequest.getTitle(),
                noticeCommentRequest.getContent(),
                noticeCommentRequest.getNoticeId(),
                user
        );

        // 저장
        NoticeCommentEntity noticeCommentEntity = noticeCommentRepository.save(createNoticeComments);

        // NoticeResponse 생성 및 반환
        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, NoticeCommentResponse.from(noticeCommentEntity));
    }

    @Transactional
    public ResponseField<NoticeCommentResponse> updateNoticeComments(NoticeCommentUpdateRequest noticeCommentUpdateRequest, String userId) {

        NoticeCommentEntity noticeCommentEntity = noticeCommentRepository.findById(noticeCommentUpdateRequest.getCommentId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!noticeCommentEntity.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }
        
        noticeCommentEntity.update(
                noticeCommentUpdateRequest.getCommentId(),
                noticeCommentUpdateRequest.getNoticeId(),
                noticeCommentUpdateRequest.getTitle(),
                noticeCommentUpdateRequest.getContent()


        );

        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, NoticeCommentResponse.from(noticeCommentEntity));
    }

    /**
     * 클래스 삭제
     * @param commentId
     */
    @Transactional
    public void deleteCommentNotice(String commentId, String userId) {
        NoticeCommentEntity noticeCommentEntity = noticeCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!noticeCommentEntity.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }
        
        noticeCommentRepository.delete(noticeCommentEntity);
    }

}
