package org.example.plain.domain.calendar.controller;

import lombok.RequiredArgsConstructor;
import org.example.plain.common.ResponseField;
import org.example.plain.common.config.SecurityUtils;
import org.example.plain.common.enums.Category;
import org.example.plain.domain.calendar.dto.CalendarRequest;
import org.example.plain.domain.calendar.dto.CalendarResponse;
import org.example.plain.domain.calendar.service.CalendarService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @PostMapping("/insert")
    public ResponseField<CalendarResponse> insertCalendar(@RequestBody CalendarRequest calendarRequest) {
        // 현재 로그인한 사용자 정보 가져오기
        String userId = SecurityUtils.getUserId();
        calendarRequest.setUserId(userId);
        return calendarService.insertCalendar(calendarRequest);
    }

    @PutMapping("/update")
    public ResponseField<CalendarResponse> updateCalendar(@RequestBody CalendarRequest calendarRequest) {
        // 현재 로그인한 사용자 정보 가져오기
        String userId = SecurityUtils.getUserId();
        calendarRequest.setUserId(userId);
        return calendarService.updateCalendar(calendarRequest);
    }

    @GetMapping("/List/{category}")
    public ResponseField<List<CalendarResponse>> getCalendar(@PathVariable Category category) {
        // 현재 로그인한 사용자 정보 가져오기
        String userId = SecurityUtils.getUserId();
        return calendarService.getCalendar(category);
    }

    @GetMapping("/List/detail/{calId}")
    public ResponseField<CalendarResponse> getDetailCalendar(@PathVariable Long calId) {
        // 현재 로그인한 사용자 정보 가져오기
        String userId = SecurityUtils.getUserId();
        return calendarService.getDetailCalendar(calId);
    }

    @DeleteMapping("/delete/{calId}")
    public ResponseEntity<Void> deleteCalendar(@PathVariable Long calId) {
        // 현재 로그인한 사용자 정보 가져오기
        String userId = SecurityUtils.getUserId();
        calendarService.deleteCalendar(calId);
        return ResponseEntity.ok().build();
    }
}
