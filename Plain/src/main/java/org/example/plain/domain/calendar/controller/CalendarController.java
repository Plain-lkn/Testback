package org.example.plain.domain.calendar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Class controller api", description = "캘린더 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/calendar")
public class CalendarController {

    private final CalendarService calendarService;
    @Operation(summary = "캘린더 생성")
    @PostMapping("/insert")
    public ResponseEntity<ResponseField<CalendarResponse>> insertCalendar(
            @RequestBody CalendarRequest calendarRequest) {

        ResponseField<CalendarResponse> responseBody = calendarService.insertCalendar(calendarRequest, SecurityUtils.getUserId());

        return ResponseEntity.status(responseBody.getStatus()).body(responseBody);
    }
    @Operation(summary = "캘린더 수정")
    @PatchMapping("/update/{calId}")
    public ResponseEntity<ResponseField<CalendarResponse>> updateCalendar(
            @RequestBody CalendarRequest calendarRequest) {

        ResponseField<CalendarResponse> responseBody = calendarService.updateCalendar(calendarRequest, SecurityUtils.getUserId());

        return ResponseEntity.status(responseBody.getStatus()).body(responseBody);
    }
    @Operation(summary = "캘린더 삭제")
    @DeleteMapping("/delete/{calId}")
    public void deleteCalendar(
            @PathVariable Long calId) {

            calendarService.deleteCalendar(calId, SecurityUtils.getUserId());
    }
    @Operation(summary = "캘린더 목록보기")
    @GetMapping("/List/{category}")
    public ResponseEntity<ResponseField<List<CalendarResponse>>> getCalendar(@PathVariable Category category){

        ResponseField<List<CalendarResponse>> responseBody = calendarService.getCalendar(category);

        return ResponseEntity.status(responseBody.getStatus()).body(responseBody);

    }
    @Operation(summary = "캘린더 상세")
    @GetMapping("/List/{calId}")
    public ResponseEntity<ResponseField<CalendarResponse>> getDetailCalendar(
            @PathVariable Long calId) {

        ResponseField<CalendarResponse> responseBody = calendarService.getDetailCalendar(calId);

        return ResponseEntity.status(responseBody.getStatus()).body(responseBody);

    }

}
