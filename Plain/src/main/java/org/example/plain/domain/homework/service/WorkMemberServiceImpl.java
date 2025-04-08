package org.example.plain.domain.homework.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.plain.domain.classMember.entity.ClassMember;
import org.example.plain.domain.classMember.entity.ClassMemberId;
import org.example.plain.domain.classMember.repository.ClassMemberRepository;
import org.example.plain.domain.homework.entity.WorkMemberId;
import org.example.plain.domain.homework.interfaces.WorkMemberService;
import org.example.plain.domain.homework.dto.WorkMember;
import org.example.plain.domain.homework.entity.WorkEntity;
import org.example.plain.domain.homework.entity.WorkMemberEntity;
import org.example.plain.repository.BoardRepository;
import org.example.plain.repository.WorkMemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkMemberServiceImpl implements WorkMemberService {

    private final WorkMemberRepository workMemberRepository;
    private final BoardRepository boardRepository;
    private final ClassMemberRepository classMemberRepository;

    @Override
    @Transactional
    public void addHomeworkMember(String workId, String memberId, String userId) {
        WorkEntity work = boardRepository.findByWorkId(workId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "과제를 찾을 수 없습니다."));

        // 권한 체크
        checkLeader(work.getClassId(), userId);

        if (work.getUserId().equals(memberId)) {
            throw new HttpClientErrorException(HttpStatusCode.valueOf(400), "생성자에게 과제를 할당할 수 없습니다.");
        }

        // 이미 과제가 할당되어 있는지 확인
        if (workMemberRepository.existsById(new WorkMemberId(workId, memberId))) {
            throw new HttpClientErrorException(HttpStatusCode.valueOf(400), "이미 과제가 할당된 사용자입니다.");
        }

        ClassMember classMember = classMemberRepository.findById(new ClassMemberId(work.getClassId(), memberId))
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "클래스 멤버가 아닙니다."));

        WorkMemberEntity workMemberEntity = WorkMemberEntity.makeWorkMemberEntity(classMember.getUser(), work);
        workMemberRepository.save(workMemberEntity);
        
        log.info("과제 멤버 추가 성공 - 과제ID: {}, 멤버ID: {}", workId, memberId);
    }

    @Override
    @Transactional
    public void removeHomeworkMember(String workId, String memberId, String userId) {
        WorkEntity work = boardRepository.findByWorkId(workId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "과제를 찾을 수 없습니다."));

        // 권한 체크
        checkLeader(work.getClassId(), userId);

        WorkMemberEntity workMemberEntity = workMemberRepository.findByWorkIdAndUserId(workId, memberId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "과제 멤버를 찾을 수 없습니다."));

        workMemberRepository.delete(workMemberEntity);
        log.info("과제 멤버 제거 성공 - 과제ID: {}, 멤버ID: {}", workId, memberId);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkMember getSingleMembers(String workId, String memberId) {
        WorkMemberEntity workMemberEntity = workMemberRepository.findByWorkIdAndUserId(workId, memberId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "과제 멤버를 찾을 수 없습니다."));
        return WorkMember.changeEntity(workMemberEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkMember> homeworkMembers(String workId) {
        WorkEntity work = boardRepository.findByWorkId(workId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "과제를 찾을 수 없습니다."));
        
        return workMemberRepository.findByWork(work).stream()
                .map(WorkMember::changeEntity)
                .toList();
    }

    private void checkLeader(String classId, String userId) {
        ClassMember classMember = classMemberRepository.findById(new ClassMemberId(classId, userId))
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "수업 멤버를 찾을 수 없습니다."));
        
        if (!classMember.getUser().equals(classMember.getClassLecture().getInstructor())) {
            throw new HttpClientErrorException(HttpStatusCode.valueOf(403), "권한이 없습니다.");
        }
    }
}
