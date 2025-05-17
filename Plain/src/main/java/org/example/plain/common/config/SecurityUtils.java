package org.example.plain.common.config;

import org.example.plain.common.enums.Role;
import org.example.plain.domain.user.dto.CustomUserDetails;
import org.example.plain.domain.user.entity.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    
    private static final String TEST_USER_ID = "test_user_id";
    private static final String TEST_USERNAME = "test_user";
    
    private static void setTestAuthentication() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            User testUser = User.builder()
                    .id(TEST_USER_ID)
                    .username(TEST_USERNAME)
                    .email("test@example.com")
                    .password("test_password")
                    .role(Role.NORMAL)
                    .build();
            
            CustomUserDetails userDetails = new CustomUserDetails(testUser);
            Authentication testAuth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            
            SecurityContextHolder.getContext().setAuthentication(testAuth);
        }
    }
    
    public static String getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            setTestAuthentication();
            authentication = SecurityContextHolder.getContext().getAuthentication();
        }
        return ((CustomUserDetails) authentication.getPrincipal()).getUser().getId();
    }

    public static String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            setTestAuthentication();
            authentication = SecurityContextHolder.getContext().getAuthentication();
        }
        return ((CustomUserDetails) authentication.getPrincipal()).getUser().getUsername();
    }
}
