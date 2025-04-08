package org.example.plain.domain.user.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.plain.domain.user.dto.CustomOAuth2User;
import org.example.plain.domain.user.service.JWTUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        if(customOAuth2User.getUserRequest().getId() == null) {
            String body = objectMapper.writeValueAsString(customOAuth2User);

            response.setContentType("application/json");
            response.getWriter().write(body);
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.sendRedirect("http://localhost:3000/sign_up");
        }else{
            String token = jwtUtil.makeJwtToken(customOAuth2User.getUserRequest().getId(), customOAuth2User.getName());
            String refresh = jwtUtil.makeRefreshToken(customOAuth2User.getUserRequest().getId(),  customOAuth2User.getName());

            Cookie cookie = makeCookie(refresh);
            response.addHeader("Authorization",token);
            response.addCookie(cookie);
            response.setStatus(HttpServletResponse.SC_OK);
            response.sendRedirect("/");
        }
    }

    private Cookie makeCookie(String token){
        Cookie cookie = new Cookie("token_refresh",token);
        cookie.setDomain("localhost");
        cookie.setPath("/");
        cookie.setMaxAge(60*60*24);
        cookie.setSecure(true);
        return cookie;
    }
}
