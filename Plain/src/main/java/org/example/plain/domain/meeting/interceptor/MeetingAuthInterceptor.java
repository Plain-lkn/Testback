package org.example.plain.domain.meeting.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.plain.domain.meeting.service.MeetingService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class MeetingAuthInterceptor implements HandlerInterceptor {

    private final MeetingService meetingService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 현재 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // 인증되지 않은 사용자 처리
        if (authentication == null || !authentication.isAuthenticated()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증되지 않은 사용자입니다.");
            return false;
        }
        
        // 사용자 정보 추출
        String userId = authentication.getName();
        String userName = authentication.getDetails() != null ? 
                authentication.getDetails().toString() : userId;

        // 컨트롤러에 적용되지 않게 해야함.
        // 요청 파라미터에서 roomId 추출
        String roomId = request.getParameter("roomId");
        if (roomId == null || roomId.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "회의실 ID가 필요합니다.");
            return false;
        }
        
        // 회의실 입장 권한 확인 로직 (필요에 따라 추가)
        // 예: 특정 회의실에 대한 접근 권한 확인
        
        // 요청 속성에 사용자 정보 저장 (컨트롤러에서 사용 가능)
        request.setAttribute("userId", userId);
        request.setAttribute("userName", userName);
        request.setAttribute("roomId", roomId);
        
        return true;
    }
} 