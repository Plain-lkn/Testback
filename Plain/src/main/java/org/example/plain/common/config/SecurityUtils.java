package org.example.plain.common.config;

import org.example.plain.domain.user.dto.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static String getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new IllegalArgumentException("No authentication information found");
        }
        return ((CustomUserDetails) authentication.getPrincipal()).getUser().getId();
    }
}
