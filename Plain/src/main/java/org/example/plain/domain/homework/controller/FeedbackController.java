package org.example.plain.domain.homework.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.plain.common.ResponseField;
import org.example.plain.common.ResponseMaker;
import org.example.plain.common.config.SecurityUtils;
import org.example.plain.domain.homework.dto.request.FeedBackRequset;
import org.example.plain.domain.homework.interfaces.FeedbackService;
import org.springframework.data.relational.core.sql.In;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 과제 피드백 컨트롤러
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    /**
     * 피드백 추가 (교수용)
     */
    @PostMapping
    public ResponseEntity<Void> addFeedback(@RequestBody FeedBackRequset feedBackRequset) {
        feedbackService.addFeedback(feedBackRequset,SecurityUtils.getUserId());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
    
    /**
     * 피드백 조회
     */
    @GetMapping("/{workId}/{studentId}")
    public ResponseEntity<ResponseField<Map<String, Object>>> getFeedback(@PathVariable String workId,
                                                          @PathVariable String studentId) {

        String feedback = feedbackService.getFeedback(workId, studentId);
        int score = feedbackService.getScore(workId, studentId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("feedback", feedback);
        response.put("score", score);
        
        return new ResponseMaker<Map<String,Object>>().ok(response);
    }

    /**
     * 점수만 조회
     */
    @GetMapping("/score/{workId}/{studentId}")
    public ResponseEntity<ResponseField<Map<String, Integer>>> getScore(@PathVariable String workId,
                                                                   @PathVariable String studentId) {
        int score = feedbackService.getScore(workId, studentId);
        
        Map<String, Integer> response = new HashMap<>();
        response.put("score", score);

        return new ResponseMaker<Map<String,Integer>>().ok(response);
    }
} 