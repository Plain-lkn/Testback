package org.example.plain.domain.homework.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.plain.domain.classMember.entity.ClassMemberId;
import org.example.plain.domain.classMember.repository.ClassMemberRepository;
import org.example.plain.domain.file.dto.FileInfo;
import org.example.plain.domain.file.dto.SubmitFileData;
import org.example.plain.domain.file.entity.FileEntity;
import org.example.plain.domain.file.entity.WorkFileEntity;
import org.example.plain.domain.file.interfaces.CloudFileService;
import org.example.plain.domain.file.interfaces.FileDatabaseService;
import org.example.plain.domain.homework.dto.WorkSubmitField;
import org.example.plain.domain.homework.dto.WorkSubmitListResponse;
import org.example.plain.domain.homework.entity.WorkEntity;
import org.example.plain.domain.homework.entity.WorkMemberEntity;
import org.example.plain.domain.homework.interfaces.SubmissionService;
import org.example.plain.domain.user.entity.User;
import org.example.plain.repository.BoardRepository;
import org.example.plain.repository.WorkMemberRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SubmissionServiceImpl implements SubmissionService {

    private final CloudFileService fileService;

    private final FileDatabaseService fileDatabaseService;
    private final WorkMemberRepository workMemberRepository;
    private final ClassMemberRepository groupMemberRepository;
    private final BoardRepository boardRepository;

    public SubmissionServiceImpl(CloudFileService fileService,
                                 @Qualifier("assignmentDatabase") FileDatabaseService fileDatabaseService,
                                 WorkMemberRepository workMemberRepository,
                                 ClassMemberRepository groupMemberRepository,
                                 BoardRepository boardRepository) {
        this.fileService = fileService;
        this.fileDatabaseService = fileDatabaseService;
        this.workMemberRepository = workMemberRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.boardRepository = boardRepository;
    }

    @Override
    @Transactional
    public void submit(WorkSubmitField workSubmitField) {
        WorkMemberEntity workMemberEntity = validateWorkMember(workSubmitField.getWorkId(), workSubmitField.getUserId());
        WorkEntity work = workMemberEntity.getWork();
        
        // 마감일 체크
        //validateDeadline(work);
        
        // 클래스 멤버 검증
        User user = groupMemberRepository.findById(new ClassMemberId(work.getClassId(), workSubmitField.getUserId()))
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "클래스 멤버가 아닙니다"))
                .getUser();

        SubmitFileData submitFileData = SubmitFileData.builder()
                .userId(user)
                .workId(work)
                .build();

        if (submitFileData.getUserId() == null || submitFileData.getWorkId() == null) {
            throw new IllegalArgumentException("User ID and Work ID cannot be null");
        }

        try {
            if (workSubmitField.getFile() != null && !workSubmitField.getFile().isEmpty()){
                List<WorkFileEntity> file = new ArrayList<>();
                List<FileInfo> files = fileService.uploadFiles(
                        submitFileData,
                        workSubmitField.getFile(),
                        "assingment",
                        work.getWorkId(),
                        user.getId()
                );

                files.forEach(fileInfo -> {
                    WorkFileEntity fileEntity = (WorkFileEntity) fileDatabaseService.save(
                            fileInfo.getFilename(),
                            fileInfo.getFileUrl(),
                            submitFileData
                    );
                    file.add(fileEntity);
                });

                workMemberEntity.setFileEntities(file);
            }

            workMemberEntity.setSubmited(true);
            workMemberEntity.setLate(work.getDeadline().isBefore(LocalDateTime.now()));
            workMemberRepository.save(workMemberEntity);

            log.info("과제 제출 성공 - 과제ID: {}, 사용자ID: {}", workSubmitField.getWorkId(), workSubmitField.getUserId());
        } catch (Exception e) {
            log.error("과제 제출 실패 - 과제ID: {}, 사용자ID: {}", workSubmitField.getWorkId(), workSubmitField.getUserId(), e);
            throw new RuntimeException("과제 제출에 실패했습니다.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getSubmissionFiles(String workId, String userId) {
        WorkMemberEntity workMemberEntity = validateWorkMember(workId, userId);
        
        // Check if the user is a class member
        WorkEntity work = workMemberEntity.getWork();
        validateClassMember(work.getClassId(), userId);

        return workMemberEntity.getFileEntities().stream()
                .map(FileEntity::getFilePath)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkSubmitListResponse> getSubmissionList(String workId) {
        return workMemberRepository.findByWork(boardRepository.findByWorkId(workId).orElseThrow()).stream()
                .map(WorkSubmitListResponse::changeEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSubmitted(String workId, String userId) {
        log.info("Submit check workId: {}, userId: {}", workId, userId);
        WorkMemberEntity workMember = workMemberRepository.findByWorkIdAndUserId(workId, userId)
                .orElseThrow(() -> new IllegalArgumentException("WorkMember not found for workId: " + workId + ", userId: " + userId));
        
        // 클래스 멤버 검증
        WorkEntity work = workMember.getWork();
        validateClassMember(work.getClassId(), userId);
        return workMember.isSubmited();
    }

    @Override
    @Transactional
    public void cancelSubmission(String workId, String userId) {
        log.info("Cancel submission workId: {}, userId: {}", workId, userId);
        WorkMemberEntity workMember = workMemberRepository.findByWorkIdAndUserId(workId, userId)
                .orElseThrow(() -> new IllegalArgumentException("WorkMember not found for workId: " + workId + ", userId: " + userId));
        
        // 클래스 멤버 검증
        WorkEntity work = workMember.getWork();
        validateClassMember(work.getClassId(), userId);
        
        // 파일 삭제
        List<FileEntity> files = new ArrayList<>(workMember.getFileEntities());
        for (FileEntity file : files) {
            try {
                fileService.deleteFile(file);
                fileDatabaseService.delete(file);
            } catch (Exception e) {
                log.error("S3 파일 삭제 실패 - 파일URL: {}", file.getFilePath(), e);
            }
        }
        
        // 제출 상태 업데이트
        workMember.setSubmited(false);
        workMember.setLate(false);
        workMember.getFileEntities().clear();
        
        log.info("과제 제출 취소 성공 - 과제ID: {}, 사용자ID: {}", workId, userId);
    }
    
    /**
     * 과제 제출 정보 검증
     * @param workId 과제 ID
     * @param userId 사용자 ID
     * @return 검증된 WorkMemberEntity
     */
    private WorkMemberEntity validateWorkMember(String workId, String userId) {
        // 클래스 멤버인지 먼저 확인
        groupMemberRepository.findById(new ClassMemberId(
                workMemberRepository.findByWorkIdAndUserId(workId, userId)
                        .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "과제 제출 정보를 찾을 수 없습니다"))
                        .getWork().getClassId(), userId))
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.FORBIDDEN, "클래스 멤버가 아닙니다"));
        return workMemberRepository.findByWorkIdAndUserId(workId, userId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "과제 할당 정보를 찾을 수 없습니다."));
    }
    
    /**
     * 클래스 멤버 검증
     * @param classId 클래스 ID
     * @param userId 사용자 ID
     * @return 검증된 User
     */
    private User validateClassMember(String classId, String userId) {
        return groupMemberRepository.findById(new ClassMemberId(classId, userId))
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.FORBIDDEN, "클래스 멤버가 아닙니다"))
                .getUser();
    }
    
    /**
     * 과제 마감일 검증
     * @param work 과제 엔티티
     */
    private void validateDeadline(WorkEntity work) {
        LocalDateTime now = LocalDateTime.now();
        if (work.getDeadline().isBefore(now)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "과제 제출 기한이 지났습니다.");
        }
    }
}