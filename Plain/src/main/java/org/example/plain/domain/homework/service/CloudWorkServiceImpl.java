package org.example.plain.domain.homework.service;

import lombok.RequiredArgsConstructor;
import org.example.plain.domain.classMember.entity.ClassMember;
import org.example.plain.domain.classMember.entity.ClassMemberId;
import org.example.plain.domain.classMember.repository.ClassMemberRepository;
import org.example.plain.domain.file.entity.FileEntity;
import org.example.plain.domain.file.interfaces.CloudFileService;
import org.example.plain.domain.homework.dto.*;
import org.example.plain.domain.homework.entity.*;
import org.example.plain.domain.homework.interfaces.WorkService;
import org.example.plain.domain.homework.repository.FileRepository;
import org.example.plain.repository.BoardRepository;
import org.example.plain.repository.WorkMemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.io.File;
import java.util.*;

import static org.example.plain.common.service.UUIDService.makeUUID;

@Service
@RequiredArgsConstructor
public class CloudWorkServiceImpl implements WorkService {
    private final CloudFileService fileService;
    private final BoardRepository boardRepository;
    private final WorkMemberRepository workMemberRepository;
    private final ClassMemberRepository classMemberRepository;
    private final FileRepository fileRepository;

    @Value("${file.path}")
    private String filepath;

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
        boardRepository.save(workEntity);
    }

    @Override
    @Transactional
    public void updateWork(Work work, String workId, String userId) {
        WorkEntity workEntity = boardRepository.findByWorkId(workId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "과제를 찾을 수 없습니다."));
        
        if (!workEntity.getUser().getId().equals(userId)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "작성자가 아닙니다.");
        }
        
        work.setWorkId(workId);
        boardRepository.save(WorkEntity.chaingeWorkEntitytoWork(work, workEntity));
    }

    @Override
    @Transactional(readOnly = true)
    public Work selectWork(String workId) {
        return Work.changeWorkEntity(boardRepository.findByWorkId(workId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "과제를 찾을 수 없습니다.")));
    }

    @Transactional(readOnly = true)
    public List<Work> selectGroupWorks(String groupId) {
        return boardRepository.findByGroupId(groupId)
                .map(workEntities -> workEntities.stream()
                        .map(Work::changeWorkEntity)
                        .toList())
                .orElse(new ArrayList<>());
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
