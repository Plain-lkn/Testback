package org.example.plain.domain.user.service;


import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

@Component
public class JWTUtil {
    private SecretKey tokenScreat;

    @Value("${spring.jwt.expires}")
    private long expiresNormal;

    @Value("${spring.jwt.refresh.expires}")
    private long expiresRefresh;


    public JWTUtil(@Value("${spring.jwt.secret}")String key) {
        tokenScreat = new SecretKeySpec(key.getBytes(), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public String makeJwtToken(String id, String username){
        return "Bearer " + Jwts.builder()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()+expiresNormal))
                .claim("type","access")
                .claim("id", id)
                .claim("username", username)
                .signWith(tokenScreat)
                .compact();
    }

    public String makeRefreshToken(String id, String username){
        return Jwts.builder()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()+expiresRefresh))
                .claim("type","refresh")
                .claim("id",id)
                .claim("username", username)
                .signWith(tokenScreat)
                .compact();
    }

    public String getUsername(String token){
        return Jwts.parser().verifyWith(tokenScreat).build().parseSignedClaims(token).getPayload().get("username", String.class);
    }

    public String getId(String token){
        return Jwts.parser().verifyWith(tokenScreat).build().parseSignedClaims(token).getPayload().get("id", String.class);
    }

    public String getType(String token){
        return Jwts.parser().verifyWith(tokenScreat).build().parseSignedClaims(token).getPayload().get("type", String.class);
    }

    public boolean isExpired(String token){
        return Jwts.parser().verifyWith(tokenScreat).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }

}
