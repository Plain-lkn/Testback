package org.example.plain.domain.user.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.plain.common.ResponseField;
import org.example.plain.common.enums.Message;
import org.example.plain.domain.user.entity.RefreshToken;
import org.example.plain.domain.user.repository.RefreshTokenRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public ResponseField reissue(HttpServletRequest request, HttpServletResponse response) {
        log.info("reissue Service get request");
        Cookie[] cookie = request.getCookies();
        String token = null;
        for(Cookie c : cookie) {
            if (c.getName().equals("token")) {
                token = c.getValue();
                break;
            }
        }

        if (token == null) {
            return new ResponseField<>("토큰이 존재하지 않습니다..", HttpStatus.UNAUTHORIZED,null);
        }

        String id = jwtUtil.getId(token);
        String username = jwtUtil.getUsername(token);

        if (jwtUtil.isExpired(token) || refreshTokenRepository.findById(token).isEmpty()) {
            return new ResponseField<>("토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED,null);
        }

        if (jwtUtil.getType(token).equals("refresh")) {
            token = jwtUtil.makeRefreshToken(id, username);

            refreshTokenRepository.save(new RefreshToken(token, jwtUtil.getId(token)));
            response.addHeader("Authorization", jwtUtil.makeJwtToken(id, username));
            response.addCookie(makeCookie(token));
            return new ResponseField<>(Message.OK.name(), HttpStatus.OK, response);
        }else
            return new ResponseField<>("지원되지 않는 토큰 입니다.", HttpStatus.UNAUTHORIZED,null);

    }

    private Cookie makeCookie(String key){
        Cookie cookie = new Cookie("token",key);
        cookie.setMaxAge(60*60*24);
        cookie.setSecure(true);
        return cookie;
    }
}
