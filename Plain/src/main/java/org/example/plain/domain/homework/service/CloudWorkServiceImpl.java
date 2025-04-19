package org.example.plain.domain.homework.service;

import org.example.plain.domain.classMember.entity.ClassMember;
import org.example.plain.domain.classMember.entity.ClassMemberId;
import org.example.plain.domain.classMember.repository.ClassMemberRepository;
import org.example.plain.domain.file.dto.FileInfo;
import org.example.plain.domain.file.dto.WorkFileData;
import org.example.plain.domain.file.entity.FileEntity;
import org.example.plain.domain.file.interfaces.CloudFileService;
import org.example.plain.domain.file.interfaces.FileDatabaseService;
import org.example.plain.domain.file.repository.WorkDocFileRepository;
import org.example.plain.domain.homework.dto.*;
import org.example.plain.domain.homework.dto.response.WorkResponse;
import org.example.plain.domain.homework.entity.*;
import org.example.plain.domain.homework.interfaces.WorkService;
import org.example.plain.repository.BoardRepository;
import org.example.plain.repository.WorkMemberRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.io.File;
import java.util.*;

import static org.example.plain.common.service.UUIDService.makeUUID;

@Service
public class CloudWorkServiceImpl implements WorkService {
    private final CloudFileService fileService;
    private final BoardRepository boardRepository;
    private final WorkMemberRepository workMemberRepository;
    private final ClassMemberRepository classMemberRepository;
    private final FileDatabaseService fileDatabaseService;
    private final WorkDocFileRepository fileRepository;

    @Value("${file.path}")
    private String filepath;

    public CloudWorkServiceImpl(
            CloudFileService fileService,
            BoardRepository boardRepository,
            WorkMemberRepository workMemberRepository,
            ClassMemberRepository classMemberRepository,
            @Qualifier("workDoc") FileDatabaseService fileDatabaseService,
            WorkDocFileRepository fileRepository
    ) {
        this.fileService = fileService;
        this.boardRepository = boardRepository;
        this.workMemberRepository = workMemberRepository;
        this.classMemberRepository = classMemberRepository;
        this.fileDatabaseService = fileDatabaseService;
        this.fileRepository = fileRepository;
    }

    @Transactional
    @Override
    public void insertWork(Work work, String classId, String userId) {
        // 클래스 멤버 검증
        ClassMemberId classMemberId = new ClassMemberId(classId, userId);
        ClassMember classMember = classMemberRepository.findById(classMemberId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "클래스 멤버가 아닙니다."));

        if (!classMember.getClassLecture().getInstructor().getId().equals(userId)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
        }

        if(work.getWorkId() == null){
            work.setWorkId(makeUUID());
        }
        work.setUserId(userId);
        work.setGroupId(classId);

        WorkEntity workEntity = WorkEntity.workToWorkEntity(work);
        workEntity.setUser(classMember.getUser());
        workEntity.setGroup(classMember.getClassLecture());
        
        // 먼저 homework 엔티티를 저장
        WorkEntity savedWorkEntity = boardRepository.save(workEntity);

        // 파일이 있는 경우에만 파일 저장
        if (work.getFileList() != null && !work.getFileList().isEmpty()) {
            fileService.uploadFiles(new WorkFileData(savedWorkEntity.getWorkId()), work.getFileList(), savedWorkEntity.getWorkId(), "temp")
                    .forEach(fileInfo -> {
                        fileDatabaseService.save(fileInfo.getFilename(), fileInfo.getFileUrl(), new WorkFileData(savedWorkEntity.getWorkId()));
                    });
        }
    }

    @Override
    @Transactional
    public void updateWork(Work work, String workId, String userId) {
        WorkEntity workEntity = boardRepository.findByWorkId(workId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "과제를 찾을 수 없습니다."));
        
        if (!workEntity.getUserId().equals(userId)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "작성자가 아닙니다.");
        }

        work.setWorkId(workId);
        boardRepository.save(WorkEntity.chaingeWorkEntitytoWork(work, workEntity));


        if (work.getFileList() != null && !work.getFileList().isEmpty()) {

            fileRepository.findByWorkWorkId(work.getWorkId()).forEach(workFile -> {
                fileService.deleteFile(workFile);
                fileDatabaseService.delete(workFile);
            });

            fileService.uploadFiles(new WorkFileData(work.getWorkId()), work.getFileList(), work.getWorkId(), "temp")
                    .forEach(fileInfo -> {
                        fileDatabaseService.save(fileInfo.getFilename(), fileInfo.getFileUrl(), new WorkFileData(work.getWorkId()));
                    });
        }
    }

    @Transactional(readOnly = true)
    @Override
    public WorkResponse selectWork(String workId) {
        WorkEntity workEntity = boardRepository.findByWorkId(workId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "과제를 찾을 수 없습니다."));

        List<FileInfo> fileInfos = (List<FileInfo>) fileRepository.findByWorkWorkId(workId).stream()
                .map(file -> FileInfo.builder()
                        .filename(file.getFilename())
                        .fileUrl(file.getFilePath())
                        .build())
                .toList();

        return WorkResponse.builder()
                .workId(workEntity.getWorkId())
                .boardId(workEntity.getBoardId())
                .groupId(workEntity.getClassId())
                .writer(workEntity.getUserId())
                .title(workEntity.getTitle())
                .content(workEntity.getContent())
                .deadline(workEntity.getDeadline())
                .fileList(fileInfos) // MultipartFile 리스트는 조회 시에는 null로 설정
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public Work selectWorkDto(String workId) {
        WorkEntity workEntity = boardRepository.findByWorkId(workId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "과제를 찾을 수 없습니다."));
        return Work.changeWorkEntity(workEntity);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Work> selectGroupWorks(String groupId) {
        return boardRepository.findByGroupId(groupId)
                .map(workEntities -> workEntities.stream()
                        .map(Work::changeWorkEntity)
                        .toList())
                .orElseThrow();
    }

    @Override
    @Transactional
    public void deleteWork(String workId) {
        WorkEntity work = boardRepository.findByWorkId(workId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "과제를 찾을 수 없습니다."));
        boardRepository.delete(work);
    }

    public List<File> getWorkResults(String workId, String userId){
        WorkMemberEntity workMemberEntity = workMemberRepository.findById(new WorkMemberId(workId,userId)).orElseThrow();

        List<File> files = new ArrayList<>();
        for (FileEntity file:workMemberEntity.getFileEntities()){
            File file1 = new File(filepath+file.getFilename());
            files.add(file1);
        }
        return files;
    }
}
