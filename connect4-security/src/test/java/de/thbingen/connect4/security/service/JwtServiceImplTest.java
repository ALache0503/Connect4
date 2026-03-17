package de.thbingen.connect4.security.service;

import de.thbingen.connect4.common.model.enums.JwtTokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceImplTest {

    private final String secret = "my-super-secret-key-that-is-long-enough-for-hs256";
    private final int expiration = 3600; // 1 Stunde
    private JwtServiceImpl jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();

        ReflectionTestUtils.setField(jwtService, "secret", secret);
        ReflectionTestUtils.setField(jwtService, "expiration", expiration);

        jwtService.initKey();
    }

    @Test
    void generate_ShouldCreateValidAccessToken() {
        Long userId = 123L;

        String token = jwtService.generate(userId, JwtTokenType.ACCESS);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("123", claims.getSubject());
        assertEquals("ACCESS", claims.get("type"));

        Date exp = claims.getExpiration();
        assertNotNull(exp);
        assertTrue(exp.after(new Date()));
        assertTrue(exp.getTime() <= System.currentTimeMillis() + (expiration * 1000) + 2000);
    }

    @Test
    void generate_ShouldCreateValidRefreshToken() {
        Long userId = 456L;

        String token = jwtService.generate(userId, JwtTokenType.REFRESH);

        assertNotNull(token);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("456", claims.getSubject());
        assertEquals("REFRESH", claims.get("type"));

        Date exp = claims.getExpiration();
        long expectedDuration = expiration * 1000L * 96;

        assertTrue(exp.getTime() > System.currentTimeMillis() + expiration * 1000);
        assertTrue(exp.getTime() <= System.currentTimeMillis() + expectedDuration + 5000);
    }
}
