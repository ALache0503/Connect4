package de.thbingen.connect4.security.adapters.in;

import de.thbingen.connect4.common.exception.UsernameTakenException;
import de.thbingen.connect4.common.model.dto.AuthRequest;
import de.thbingen.connect4.common.model.dto.AuthResponse;
import de.thbingen.connect4.security.exception.LoginFailedException;
import de.thbingen.connect4.security.ports.in.SecurityService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.WebUtils;

import java.time.Duration;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthenticationRestController {
    private final SecurityService securityService;

    @Value("${jwt.expiration}")
    private Integer jwtExpiration;

    @PostMapping(value = "/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRequest request, HttpServletResponse response) {
        AuthResponse authResponse = securityService.register(request);

        setAuthTokens(authResponse, response);

        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request, HttpServletResponse response) {
        AuthResponse authResponse = securityService.login(request);

        setAuthTokens(authResponse, response);

        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        Cookie refreshToken = WebUtils.getCookie(request, "REFRESH_TOKEN");

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AuthResponse authResponse = securityService.refresh(refreshToken.getValue());

        setAuthTokens(authResponse, response);

        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(UsernameTakenException.class)
    public ResponseEntity<Map<String, String>> handleUsernameTaken(UsernameTakenException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<Map<String, String>> handleLoginFailed(LoginFailedException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Map<String, String>> handleJwtException(JwtException ignore) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid Token"));
    }

    private void setAuthTokens(AuthResponse authResponse, HttpServletResponse response) {
        ResponseCookie accessCookie = ResponseCookie.from("ACCESS_TOKEN", authResponse.getAccessToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofSeconds(jwtExpiration))
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN", authResponse.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/api/v1/auth/refresh")
                .sameSite("Lax")
                .maxAge(Duration.ofSeconds(jwtExpiration * 96L))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }
}
