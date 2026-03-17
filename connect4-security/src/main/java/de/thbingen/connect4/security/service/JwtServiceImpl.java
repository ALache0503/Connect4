package de.thbingen.connect4.security.service;

import de.thbingen.connect4.common.model.enums.JwtTokenType;
import de.thbingen.connect4.security.ports.in.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
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
}
