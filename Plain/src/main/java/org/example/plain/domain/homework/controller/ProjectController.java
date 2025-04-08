package org.example.plain.domain.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.plain.common.ResponseField;
import org.example.plain.common.ResponseMaker;
import org.example.plain.common.config.SecurityUtils;
import org.example.plain.domain.board.service.BoardServiceImpl;
import org.example.plain.domain.file.interfaces.FileService;
import org.example.plain.domain.homework.dto.*;
import org.example.plain.domain.homework.interfaces.SubmissionService;
import org.example.plain.domain.homework.interfaces.WorkMemberService;
import org.example.plain.domain.homework.interfaces.WorkService;
import org.example.plain.domain.homework.service.WorkMemberServiceImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "homework", description = "과제 관리 API")
@RestController
@RequestMapping("/api/v1/classes/{classId}/assignments")
@RequiredArgsConstructor
public class ProjectController {

    private final WorkService workService;
    private final WorkMemberServiceImpl workMemberService;
    private final BoardServiceImpl boardService;
    private final SubmissionService submissionService;

    @Operation(summary = "새 과제 생성")
    @PostMapping
    public ResponseEntity<ResponseField> createAssignment(
            @Parameter(description = "수업 ID") @PathVariable String classId,
            @Parameter(description = "과제 정보") @RequestBody Work work) {
        workService.insertWork(work, classId, SecurityUtils.getUserId());
        return new ResponseMaker<Void>().noContent();
    }

    @Operation(summary = "수업별 과제 목록 조회")
    @GetMapping
    public ResponseEntity<ResponseField<List<Work>>> getAssignmentList(
            @Parameter(description = "수업 ID") @PathVariable String classId) {
        return new ResponseMaker<List<Work>>().ok(workService.selectGroupWorks(classId));
    }

    @Operation(summary = "과제 상세 정보 조회")
    @GetMapping("/{assignmentId}")
    public ResponseEntity<ResponseField<Work>> getAssignmentDetail(
            @Parameter(description = "수업 ID") @PathVariable String classId,
            @Parameter(description = "과제 ID") @PathVariable String assignmentId) {
        Work work = workService.selectWork(assignmentId);
        return new ResponseMaker<Work>().ok(work);
    }

    @Operation(summary = "과제 수정")
    @PutMapping("/{assignmentId}")
    public ResponseEntity<ResponseField> updateAssignment(
            @Parameter(description = "수업 ID") @PathVariable String classId,
            @Parameter(description = "과제 ID") @PathVariable String assignmentId,
            @Parameter(description = "과제 정보") @RequestBody Work work) {
        workService.updateWork(work, assignmentId, SecurityUtils.getUserId());
        return new ResponseMaker<Void>().noContent();
    }

    @Operation(summary = "과제 삭제")
    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<ResponseField> deleteAssignment(
            @Parameter(description = "수업 ID") @PathVariable String classId,
            @Parameter(description = "과제 ID") @PathVariable String assignmentId) {
        workService.deleteWork(assignmentId);
        return new ResponseMaker<Void>().noContent();
    }

    @Operation(summary = "과제 제출자 목록 조회")
    @GetMapping("/{assignmentId}/members")
    public ResponseEntity<ResponseField<List<WorkMember>>> getAssignmentMembers(
            @Parameter(description = "수업 ID") @PathVariable String classId,
            @Parameter(description = "과제 ID") @PathVariable String assignmentId) {
        return new ResponseMaker<List<WorkMember>>().ok(workMemberService.homeworkMembers(assignmentId));
    }

    @Operation(summary = "과제 멤버 추가")
    @PostMapping("/{assignmentId}/members/{userId}")
    public ResponseEntity<ResponseField> addAssignmentMember(
            @Parameter(description = "수업 ID") @PathVariable String classId,
            @Parameter(description = "과제 ID") @PathVariable String assignmentId,
            @Parameter(description = "사용자 ID") @PathVariable String userId) {
        workMemberService.addHomeworkMember(assignmentId, userId, SecurityUtils.getUserId());
        return new ResponseMaker<Void>().noContent();
    }

    @Operation(summary = "과제 멤버 제거")
    @DeleteMapping("/{assignmentId}/members/{userId}")
    public ResponseEntity<ResponseField> removeAssignmentMember(
            @Parameter(description = "수업 ID") @PathVariable String classId,
            @Parameter(description = "과제 ID") @PathVariable String assignmentId,
            @Parameter(description = "사용자 ID") @PathVariable String userId) {
        workMemberService.removeHomeworkMember(assignmentId, userId, SecurityUtils.getUserId());
        return new ResponseMaker<Void>().noContent();
    }

    @Operation(summary = "과제 제출 목록 조회")
    @GetMapping("/{assignmentId}/submissions")
    public ResponseEntity<ResponseField<List<WorkSubmitListResponse>>> getAssignmentSubmissions(
            @Parameter(description = "수업 ID") @PathVariable String classId,
            @Parameter(description = "과제 ID") @PathVariable String assignmentId) {
        return new ResponseMaker<List<WorkSubmitListResponse>>().ok(submissionService.getSubmissionList(assignmentId));
    }

    @Operation(summary = "사용자의 과제 제출 파일 URL 조회")
    @GetMapping("/{assignmentId}/submissions/{userId}")
    public ResponseEntity<ResponseField<List<String>>> getUserSubmissions(
            @Parameter(description = "수업 ID") @PathVariable String classId,
            @Parameter(description = "과제 ID") @PathVariable String assignmentId,
            @Parameter(description = "사용자 ID") @PathVariable String userId) {
        return new ResponseMaker<List<String>>().ok(submissionService.getSubmissionFiles(assignmentId, userId));
    }

    @Operation(summary = "내 과제 제출물 조회")
    @GetMapping("/{assignmentId}/submissions/me")
    public ResponseEntity<ResponseField<List<String>>> getMySubmissions(
            @Parameter(description = "수업 ID") @PathVariable String classId,
            @Parameter(description = "과제 ID") @PathVariable String assignmentId) {
        return new ResponseMaker<List<String>>().ok(submissionService.getSubmissionFiles(assignmentId, SecurityUtils.getUserId()));
    }

    @Operation(summary = "과제 제출 상태 확인")
    @GetMapping("/{assignmentId}/submissions/me/status")
    public ResponseEntity<ResponseField<Boolean>> checkSubmissionStatus(
            @Parameter(description = "수업 ID") @PathVariable String classId,
            @Parameter(description = "과제 ID") @PathVariable String assignmentId) {
        return new ResponseMaker<Boolean>().ok(submissionService.isSubmitted(assignmentId, SecurityUtils.getUserId()));
    }

    @Operation(summary = "과제 제출")
    @PostMapping(value = "/{assignmentId}/submissions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseField<Void>> submitAssignment(
            @Parameter(description = "수업 ID") @PathVariable String classId,
            @Parameter(description = "과제 ID") @PathVariable String assignmentId,
            @Parameter(description = "제출 정보") @RequestPart WorkSubmitField workSubmitField) {
        workSubmitField.setUserId(SecurityUtils.getUserId());
        workSubmitField.setWorkId(assignmentId);
        submissionService.submit(workSubmitField);
        return new ResponseMaker<Void>().ok(null);
    }

    @Operation(summary = "과제 제출 취소")
    @DeleteMapping("/{assignmentId}/submissions/me")
    public ResponseEntity<ResponseField> cancelSubmission(
            @Parameter(description = "수업 ID") @PathVariable String classId,
            @Parameter(description = "과제 ID") @PathVariable String assignmentId) {
        submissionService.cancelSubmission(assignmentId, SecurityUtils.getUserId());
        return new ResponseMaker<Void>().noContent();
    }
}
