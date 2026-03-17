package de.thbingen.connect4.common.service;

import de.thbingen.connect4.common.model.enums.JwtTokenType;
import de.thbingen.connect4.common.ports.in.JwtUtilService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilServiceImplTest {

    private static final String TEST_SECRET = "supersecrettestkeymustbelongenough123456";
    private JwtUtilService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtUtilServiceImpl();

        // Fakes @Value("${jwt.secret}")
        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);

        // Execute @PostConstruct Methode
        ReflectionTestUtils.invokeMethod(jwtService, "initKey");
    }

    @Test
    void extractUserId_ShouldReturnCorrectSubject() {
        String expectedUser = "testUser";
        String token = createTestToken(expectedUser, 10000, JwtTokenType.ACCESS);

        String result = jwtService.extractUserId(token);

        assertEquals(expectedUser, result);
    }

    @Test
    void validateToken_ShouldReturnTrue_WhenTokenIsValid() {
        String token = createTestToken("user", 10000, JwtTokenType.ACCESS);

        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsExpired() {
        String token = createTestToken("user", -1000, JwtTokenType.ACCESS);

        try {
            assertFalse(jwtService.validateToken(token));
        } catch (ExpiredJwtException e) {
            assertTrue(true);
        }
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenTypeIsWrong() {
        String token = createTestToken("user", 10000, JwtTokenType.REFRESH);

        assertFalse(jwtService.validateToken(token));
    }

    @Test
    void isAccessToken_ShouldReturnTrue_ForAccessToken() {
        String token = createTestToken("user", 10000, JwtTokenType.ACCESS);

        assertTrue(jwtService.isAccessToken(token));
    }

    @Test
    void isAccessToken_ShouldReturnFalse_ForRefreshToken() {
        String token = createTestToken("user", 10000, JwtTokenType.REFRESH);

        assertFalse(jwtService.isAccessToken(token));
    }

    private String createTestToken(String subject, long expirationMillis, JwtTokenType tokenType) {
        return Jwts.builder()
                .subject(subject)
                .claim("type", tokenType)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}