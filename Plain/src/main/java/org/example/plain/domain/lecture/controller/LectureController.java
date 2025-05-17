package org.example.plain.domain.lecture.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.plain.common.ResponseField;
import org.example.plain.common.config.SecurityUtils;
import org.example.plain.domain.lecture.dto.*;
import org.example.plain.domain.lecture.dto.lecturecurriculum.LectureCategoryResponse;
import org.example.plain.domain.lecture.service.interfaces.*;
import org.example.plain.domain.user.dto.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "Lecture controller api", description = "강의 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/lectures")
public class LectureController {

    private final LectureService lectureService;
    private final LectureMemberService lectureMemberService;
    private final LectureRevenueService lectureRevenueService;
    private final LectureFileService lectureFileService;
    //private final LectureCategoryService lectureCategoryService;

    @Operation(summary = "강의목록 검색")
    @GetMapping("/shop/list")
    public ResponseEntity<ResponseField<List<LectureResponse>>> getLectureList() {
        return ResponseEntity.ok(lectureService.getLectureList());
    }

    @Operation(summary = "수강자 목록 검색")
    @GetMapping("/{lecture_id}/students")
    public ResponseEntity<ResponseField<List<UserResponse>>> getStudentList(@PathVariable("lecture_id") String lectureId) {
        return ResponseEntity.ok(lectureMemberService.getStudentList(lectureId));
    }

    @Operation(summary = "강의세부정보 반환(상점)")
    @GetMapping("/shop/{lecture_id}")
    public ResponseEntity<ResponseField<LectureResponse>> getLectureDetailShop(
            @PathVariable("lecture_id") String lectureId) {
        return ResponseEntity.ok(lectureService.getLectureDetailShop(lectureId));
    }

    @Operation(summary = "강의 상세 정보")
    @GetMapping("/{lecture_id}")
    public ResponseEntity<ResponseField<LectureResponse>> getLectureDetail(
            @PathVariable("lecture_id") String lectureId) {
        return ResponseEntity.ok(lectureService.getLectureDetail(lectureId));
    }

    @Operation(summary = "강의 생성")
    @PostMapping
    public ResponseEntity<ResponseField<LectureResponse>> createLecture(
            @RequestBody LectureRequest request) {
        return ResponseEntity.ok(lectureService.createLecture(request, SecurityUtils.getUserId()));
    }

    @Operation(summary = "강의 수정")
    @PutMapping("/{lecture_id}")
    public ResponseEntity<ResponseField<LectureResponse>> updateLecture(
            @PathVariable("lecture_id") String lectureId,
            @RequestBody LectureRequest request) {
        return ResponseEntity.ok(lectureService.updateLecture(lectureId, request, SecurityUtils.getUserId()));
    }

    @Operation(summary = "강의 삭제")
    @DeleteMapping("/{lecture_id}")
    public ResponseEntity<Void> deleteLecture(@PathVariable("lecture_id") String lectureId) {
        lectureService.deleteLecture(lectureId, SecurityUtils.getUserId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "강의 결제 승인")
    @PostMapping("/{lecture_id}/payment")
    public ResponseEntity<ResponseField<Boolean>> processLecturePayment(
            @PathVariable("lecture_id") String lectureId,
            @RequestParam("paymentType") String paymentType,
            @RequestParam("orderId") String orderId,
            @RequestParam("paymentKey") String paymentKey,
            @RequestParam("amount") Integer amount) {
        return ResponseEntity.ok(lectureRevenueService.processLecturePayment(paymentType, orderId, paymentKey, amount));
    }

    @Operation(summary = "강의 수익 정보 조회")
    @GetMapping("/{lecture_id}/profit")
    public ResponseEntity<ResponseField<Integer>> getLectureProfit(
            @PathVariable("lecture_id") String lectureId) {
        return ResponseEntity.ok(lectureRevenueService.getLectureProfit(lectureId));
    }

    @Operation(summary = "결제 실패 처리")
    @PostMapping("/payment/fail")
    public ResponseEntity<ResponseField<Void>> handlePaymentFailure(
            @RequestParam("errorCode") String errorCode,
            @RequestParam("errorMessage") String errorMessage,
            @RequestParam("orderId") String orderId) {
        return ResponseEntity.ok(lectureRevenueService.handlePaymentFailure(errorCode, errorMessage, orderId));
    }

    @Operation(summary = "수강 목록 조회")
    @GetMapping("/my")
    public ResponseEntity<ResponseField<List<LectureResponse>>> getLectureMyList() {
        return ResponseEntity.ok(lectureMemberService.getLectureMyList(SecurityUtils.getUserId()));
    }

    @Operation(summary = "수강 신청")
    @PostMapping("/{lecture_id}/enroll")
    public ResponseEntity<ResponseField<Void>> enrollLecture(
            @PathVariable("lecture_id") String lectureId) {
        return ResponseEntity.ok(lectureMemberService.enrollLecture(lectureId, SecurityUtils.getUserId()));
    }

    @Operation(summary = "수강 취소")
    @DeleteMapping("/{lecture_id}/enroll")
    public ResponseEntity<ResponseField<Void>> cancelEnrollment(
            @PathVariable("lecture_id") String lectureId) {
        return ResponseEntity.ok(lectureMemberService.cancelEnrollment(lectureId, SecurityUtils.getUserId()));
    }

    @Operation(summary = "강의 파일 업로드")
    @PostMapping("/{lecture_id}/files")
    public ResponseEntity<ResponseField<String>> uploadLectureFile(
            @PathVariable("lecture_id") String lectureId,
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(lectureFileService.uploadLectureFile(lectureId, file));
    }

    @Operation(summary = "강의 파일 조회")
    @GetMapping("/{lecture_id}/files/{file_id}")
    public ResponseEntity<ResponseField<String>> getLectureFile(
            @PathVariable("lecture_id") String lectureId,
            @PathVariable("file_id") String fileId) {
        return ResponseEntity.ok(lectureFileService.getLectureFile(lectureId, fileId));
    }

    @Operation(summary = "강의 파일 목록 조회")
    @GetMapping("/{lecture_id}/files")
    public ResponseEntity<ResponseField<List<LectureFileResponse>>> getLectureFiles(
            @PathVariable("lecture_id") String lectureId) {
        return ResponseEntity.ok(lectureFileService.getLectureFiles(lectureId));
    }

    @Operation(summary = "강의 파일 삭제")
    @DeleteMapping("/{lecture_id}/files/{file_id}")
    public ResponseEntity<ResponseField<Void>> deleteLectureFile(
            @PathVariable("lecture_id") String lectureId,
            @PathVariable("file_id") String fileId) {
        return ResponseEntity.ok(lectureFileService.deleteLectureFile(lectureId, fileId));
    }

    @Operation(summary = "녹화영상 조회")
    @GetMapping("/{lecture_id}/videos/{video_id}")
    public ResponseEntity<ResponseField<String>> getLectureVideo(
            @PathVariable("lecture_id") String lectureId,
            @PathVariable("video_id") String videoId) {
        return ResponseEntity.ok(lectureFileService.getLectureVideo(lectureId, SecurityUtils.getUserId(), videoId));
    }

//    @Operation(summary = "카테고리 목록 조회")
//    @GetMapping("/categories")
//    public ResponseEntity<ResponseField<List<LectureCategoryResponse>>> getCategoryList() {
//        return ResponseEntity.ok(lectureCategoryService.getCategoryList());
//    }
//
//    @Operation(summary = "카테고리별 강의 목록 조회")
//    @GetMapping("/categories/{category_id}")
//    public ResponseEntity<ResponseField<List<LectureResponse>>> getLecturesByCategory(
//            @PathVariable("category_id") String categoryId) {
//        return ResponseEntity.ok(lectureCategoryService.getLecturesByCategory(categoryId));
//    }
//
//    @Operation(summary = "강의 카테고리 설정")
//    @PostMapping("/{lecture_id}/category/{category_id}")
//    public ResponseEntity<ResponseField<Void>> setLectureCategory(
//            @PathVariable("lecture_id") String lectureId,
//            @PathVariable("category_id") String categoryId) {
//        return ResponseEntity.ok(lectureCategoryService.setLectureCategory(lectureId, categoryId));
//    }
}
