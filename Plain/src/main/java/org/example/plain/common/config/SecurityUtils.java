package org.example.plain.common.config;

import org.example.plain.domain.user.dto.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static String getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new IllegalArgumentException("유저 정보가 없습니다.");
        }
        return ((CustomUserDetails) authentication.getPrincipal()).getUser().getId();
    }

    public static String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new IllegalArgumentException("유저 정보가 없습니다.");
        }
        return ((CustomUserDetails) authentication.getPrincipal()).getUser().getUsername();
    }
}
