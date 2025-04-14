package org.example.plain.domain.user.filters;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.plain.common.ResponseField;
import org.example.plain.common.ResponseMaker;
import org.example.plain.domain.user.dto.CustomUserDetails;
import org.example.plain.domain.user.dto.TokenResponse;
import org.example.plain.domain.user.dto.UserLoginRequest;
import org.example.plain.domain.user.dto.UserRequest;
import org.example.plain.domain.user.entity.RefreshToken;
import org.example.plain.domain.user.repository.RefreshTokenRepository;
import org.example.plain.domain.user.service.JWTUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final RefreshTokenRepository repository;
    private final JWTUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            UserLoginRequest userRequest = objectMapper.readValue(request.getInputStream(), UserLoginRequest.class);
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userRequest.id(), userRequest.password());
            return authenticationManager.authenticate(token);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        CustomUserDetails customUserDetails = (CustomUserDetails) authResult.getPrincipal();
        String token = jwtUtil.makeJwtToken(customUserDetails.getUser().getId(), customUserDetails.getUser().getUsername());
        String refresh = jwtUtil.makeRefreshToken(customUserDetails.getUser().getId(), customUserDetails.getUser().getUsername());

        repository.save(new RefreshToken(refresh, customUserDetails.getUser().getId()));

        response.addHeader("Authorization",token);
        response.setContentType("application/json");

        response.getWriter()
                .write(objectMapper.writeValueAsString(new ResponseMaker<TokenResponse>().ok(new TokenResponse(customUserDetails.getUser().getId(),token))));
        response.addCookie(makeCookie(refresh));
        response.setStatus(HttpStatus.OK.value());
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        SecurityContextHolder.getContextHolderStrategy().clearContext();
        ResponseField responseField = new ResponseField<>(failed.getMessage(), HttpStatus.UNAUTHORIZED,null);
        String body = objectMapper.writeValueAsString(responseField);
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(body);
    }

    private Cookie makeCookie(String token){
        Cookie cookie = new Cookie("token",token);
        cookie.setMaxAge(60*60*24);
        cookie.setSecure(true);
        return cookie;
    }

}
