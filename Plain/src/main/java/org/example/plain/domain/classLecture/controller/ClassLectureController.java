package org.example.plain.domain.classLecture.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.plain.common.config.SecurityUtils;
import org.example.plain.domain.classLecture.dto.ClassAddRequest;
import org.example.plain.domain.classLecture.dto.ClassMemberResponse;
import org.example.plain.domain.classLecture.dto.ClassResponse;
import org.example.plain.domain.classLecture.service.ClassJoinService;
import org.example.plain.domain.classLecture.service.ClassLectureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Class controller api", description = "클래스 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/classes")
public class ClassLectureController {

    private final ClassLectureService classLectureService;
    private final ClassJoinService classJoinService;

    @Operation(summary = "모든 클래스 조회")
    @GetMapping
    public ResponseEntity<List<ClassResponse>> getAllClass(){
        return ResponseEntity.ok().body(classLectureService.getAllClass());
    }

    @Operation(summary = "단일 클래스 조회")
    @GetMapping("/{classId}")
    public ResponseEntity<ClassResponse> getClass(
            @PathVariable String classId
    ) {
        return ResponseEntity.ok().body(classLectureService.getClass(classId));
    }

    @Operation(summary = "클래스 생성")
    @PostMapping
    public ResponseEntity<ClassResponse> createClass(
            @RequestBody ClassAddRequest classAddRequest
    ) {
        return ResponseEntity.ok()
                .body(classLectureService.createClass(classAddRequest, SecurityUtils.getUserId()));
    }

    @Operation(summary = "클래스 수정")
    @PatchMapping("/{classId}")
    public ResponseEntity<ClassResponse> updateClass(
            @PathVariable String classId,
            @RequestBody ClassAddRequest classAddRequest
    ) {
        return ResponseEntity.ok()
                .body(classLectureService.modifiedClass(classAddRequest, classId, SecurityUtils.getUserId()));
    }

    @Operation(summary = "클래스 삭제")
    @DeleteMapping("/{classId}")
    public ResponseEntity<ClassResponse> deleteClass(
            @PathVariable String classId
    ) {
        ClassResponse deleteClass = classLectureService.deleteClass(classId, SecurityUtils.getUserId());
        return ResponseEntity.ok().body(deleteClass);
    }

    @Operation(summary = "클래스 멤버 조회")
    @GetMapping("/{classId}/members")
    public ResponseEntity<List<ClassMemberResponse>> getMember(
            @PathVariable String classId
    ) {
        return ResponseEntity.ok().body(classLectureService.getClassMembers(classId));
    }

    @Operation(summary = "클래스 코드 생성")
    @PostMapping("/{classId}/invite-member")
    public ResponseEntity<String> joinCode(
            @PathVariable String classId
    ) {
        return ResponseEntity.ok().body(classLectureService.createCode(classId));
    }

    @Operation(summary = "클래스 검색")
    @PostMapping("/search")
    public ResponseEntity<List<ClassResponse>> searchClass(
            @RequestParam String query
    ) {
        return ResponseEntity.ok().body(classLectureService.searchClass(query));
    }

}
