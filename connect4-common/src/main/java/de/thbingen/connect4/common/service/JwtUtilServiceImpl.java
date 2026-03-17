package de.thbingen.connect4.common.service;

import de.thbingen.connect4.common.model.enums.JwtTokenType;
import de.thbingen.connect4.common.ports.in.JwtUtilService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtUtilServiceImpl implements JwtUtilService {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey key;

    @PostConstruct
    public void initKey() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public String extractUserId(String token) {
        return getClaims(token).getSubject();
    }

    @Override
    public boolean validateToken(String token) {
        return !isTokenExpired(token) && isAccessToken(token);
    }

    @Override
    public boolean isAccessToken(String token) {
        return getClaims(token).get("type").equals(JwtTokenType.ACCESS.toString());
    }

    @Override
    public Date extractExpiration(String token) {
        return getClaims(token).getExpiration();
    }

    private Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }
}
