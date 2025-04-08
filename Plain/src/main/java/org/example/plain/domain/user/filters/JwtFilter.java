package org.example.plain.domain.user.filters;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.plain.common.enums.Role;
import org.example.plain.domain.user.dto.CustomUserDetails;
import org.example.plain.domain.user.entity.User;
import org.example.plain.domain.user.service.JWTUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");

        if (token == null){
            filterChain.doFilter(request, response);
            return;
        }

        if(!token.startsWith("Bearer")){
            throw new IllegalArgumentException("Bearer 타입이 아닙니다.");
        }

        token = token.substring(7);

        if(!jwtUtil.getType(token).equals("access")){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println("invalid token");
        }
        if(jwtUtil.isExpired(token)){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println("token is expired");
            return;
        }

        CustomUserDetails customUserDetails = new CustomUserDetails(
                User.builder()
                        .id(jwtUtil.getId(token))
                        .username(jwtUtil.getUsername(token))
                        .role(Role.NORMAL)
                        .build()
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContextHolderStrategy().getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
