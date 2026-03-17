package de.thbingen.connect4.gateway.service;

import de.thbingen.connect4.gateway.model.enums.JwtTokenType;
import de.thbingen.connect4.gateway.ports.in.JwtUtilService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtUtilServiceImpl implements JwtUtilService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Integer expiration;

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
    public String generate(Long userId, JwtTokenType tokenType) {
        long expMillis = tokenType.equals(JwtTokenType.ACCESS) ? expiration * 1000 : expiration * 1000 * 96;

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .claim("type", tokenType.toString())
                .expiration(Date.from(Instant.now().plus(Duration.ofMillis(expMillis))))
                .signWith(key)
                .compact();
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
