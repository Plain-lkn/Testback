package org.example.plain.domain.notice.controller;

import lombok.RequiredArgsConstructor;
import org.example.plain.common.ResponseField;
import org.example.plain.domain.notice.dto.*;
import org.example.plain.domain.notice.service.NoticeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {

    private final NoticeService noticeService;

    @PostMapping("/{notice}")
    public ResponseEntity<ResponseField<NoticeResponse>> createNotice(
            @RequestBody NoticeRequest noticeRequest) {

        ResponseField<NoticeResponse> responseBody = noticeService.createNotice(noticeRequest);

        return ResponseEntity.status(responseBody.getStatus()).body(responseBody);
    }

    @PatchMapping("/{notice_id}")
    public ResponseEntity<ResponseField<NoticeResponse>> updateNotice(
            @RequestBody NoticeUpdateRequest noticeUpdateRequest) {

        ResponseField<NoticeResponse> responseBody = noticeService.updateNotice(noticeUpdateRequest);

        return ResponseEntity.status(responseBody.getStatus()).body(responseBody);
    }

    @GetMapping
    public ResponseEntity<ResponseField<List<NoticeResponse>>> getAllNotice(){

        ResponseField<List<NoticeResponse>> responseBody = noticeService.getAllNotice();

        return ResponseEntity.status(responseBody.getStatus()).body(responseBody);

    }

    @GetMapping("/{notice_id}")
    public ResponseEntity<ResponseField<NoticeResponse>> getNotice(
            @PathVariable Long noticeId) {

        ResponseField<NoticeResponse> responseBody = noticeService.getNotice(noticeId);

        return ResponseEntity.status(responseBody.getStatus()).body(responseBody);

    }

    @DeleteMapping("/{notice_id}")
    public void deleteNotice(
            @PathVariable Long noticeId) {

        noticeService.deleteNotice(noticeId);
    }
    // 댓글



    @PostMapping("/{notice_id}/comments")
    public ResponseEntity<ResponseField<NoticeCommentResponse>> createNoticeComments(
            @RequestBody NoticeCommentRequest noticeCommentRequest) {

        ResponseField<NoticeCommentResponse> responseBody = noticeService.createNoticeComments(noticeCommentRequest);

        return ResponseEntity.status(responseBody.getStatus()).body(responseBody);

    }

    @PutMapping("/{notice_id}/comments/{comment_id}")
    public ResponseEntity<ResponseField<NoticeCommentResponse>> updateNoticeComments(
            @RequestBody NoticeCommentUpdateRequest noticeCommentUpdateRequest) {

        ResponseField<NoticeCommentResponse> responseBody = noticeService.updateNoticeComments(noticeCommentUpdateRequest);
        return ResponseEntity.status(responseBody.getStatus()).body(responseBody);
    }

    @DeleteMapping("/{notice_id}/comments/{comment_id}")
    public void deleteNoticeComments(
            @PathVariable Long commentId) {

        noticeService.deleteNotice(commentId);
    }
}
