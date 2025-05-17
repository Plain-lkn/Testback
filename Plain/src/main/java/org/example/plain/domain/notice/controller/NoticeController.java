package org.example.plain.domain.notice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.plain.common.ResponseField;
import org.example.plain.common.config.SecurityUtils;
import org.example.plain.domain.notice.dto.*;
import org.example.plain.domain.notice.service.NoticeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Tag(name = "Class controller api", description = "공지사항 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "공지사항 생성")
    @PostMapping
    public ResponseEntity<ResponseField<NoticeResponse>> createNotice(
            @RequestBody NoticeRequest noticeRequest) {

        ResponseField<NoticeResponse> responseBody = noticeService.createNotice(noticeRequest, SecurityUtils.getUserId());

        return ResponseEntity.status(responseBody.getStatus()).body(responseBody);
    }
    @Operation(summary = "공지사항 수정")
    @PatchMapping("/{notice_id}")
    public ResponseEntity<ResponseField<NoticeResponse>> updateNotice(
            @RequestBody NoticeUpdateRequest noticeUpdateRequest) {

        ResponseField<NoticeResponse> responseBody = noticeService.updateNotice(noticeUpdateRequest, SecurityUtils.getUserId());

        return ResponseEntity.status(responseBody.getStatus()).body(responseBody);
    }
    @Operation(summary = "공지사항 목록검색")
    @GetMapping
    public ResponseEntity<ResponseField<List<NoticeResponse>>> getAllNotice(){

        ResponseField<List<NoticeResponse>> responseBody = noticeService.getAllNotice();

        return ResponseEntity.status(responseBody.getStatus()).body(responseBody);

    }
    @Operation(summary = "공지사항 상세 조회")
    @GetMapping("/{noticeId}")
    public ResponseEntity<ResponseField<NoticeResponse>> getNotice(
            @PathVariable String noticeId) {

        ResponseField<NoticeResponse> responseBody = noticeService.getNotice(noticeId);

        return ResponseEntity.status(responseBody.getStatus()).body(responseBody);

    }
    @Operation(summary = "공지 삭제")
    @DeleteMapping("/{noticeId}")
    public void deleteNotice(
            @PathVariable String noticeId) {

        noticeService.deleteNotice(noticeId, SecurityUtils.getUserId());
    }
    // 댓글


    @Operation(summary = "댓글 생성")
    @PostMapping("/{notice_id}/comments")
    public ResponseEntity<ResponseField<NoticeCommentResponse>> createNoticeComments(
            @RequestBody NoticeCommentRequest noticeCommentRequest) {

        ResponseField<NoticeCommentResponse> responseBody = noticeService.createNoticeComments(noticeCommentRequest, SecurityUtils.getUserId());

        return ResponseEntity.status(responseBody.getStatus()).body(responseBody);

    }
    @Operation(summary = "댓글 수정")
    @PutMapping("/{notice_id}/comments/{comment_id}")
    public ResponseEntity<ResponseField<NoticeCommentResponse>> updateNoticeComments(
            @RequestBody NoticeCommentUpdateRequest noticeCommentUpdateRequest) {

        ResponseField<NoticeCommentResponse> responseBody = noticeService.updateNoticeComments(noticeCommentUpdateRequest, SecurityUtils.getUserId());
        return ResponseEntity.status(responseBody.getStatus()).body(responseBody);
    }
    @Operation(summary = "댓글 삭제")
    @DeleteMapping("/{notice_id}/comments/{comment_id}")
    public void deleteNoticeComments(
            @PathVariable String commentId) {

        noticeService.deleteNotice(commentId, SecurityUtils.getUserId());
    }
}
