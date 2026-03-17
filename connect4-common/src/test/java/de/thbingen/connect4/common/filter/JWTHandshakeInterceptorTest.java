package de.thbingen.connect4.common.filter;

import de.thbingen.connect4.common.model.dto.UserDTO;
import de.thbingen.connect4.common.ports.in.JwtUtilService;
import de.thbingen.connect4.common.ports.out.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JWTHandshakeInterceptorTest {

    @Mock
    private JwtUtilService jwtUtilService;

    @Mock
    private UserService userService;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private ServerHttpResponse response;

    @Mock
    private WebSocketHandler wsHandler;

    @InjectMocks
    private JWTHandshakeInterceptor interceptor;

    private Map<String, Object> attributes;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        attributes = new HashMap<>();
        headers = new HttpHeaders();

        lenient().when(request.getHeaders()).thenReturn(headers);
    }

    @Test
    void beforeHandshake_ShouldReturnFalse_WhenParametersAreNull() {
        assertFalse(interceptor.beforeHandshake(null, response, wsHandler, attributes));
        assertFalse(interceptor.beforeHandshake(request, null, wsHandler, attributes));
        assertFalse(interceptor.beforeHandshake(request, response, null, attributes));
        assertFalse(interceptor.beforeHandshake(request, response, wsHandler, null));
    }

    @Test
    void beforeHandshake_ShouldReturnFalseAndUnauthorized_WhenCookieHeaderMissing() {
        // Act
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        // Assert
        assertFalse(result);
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void beforeHandshake_ShouldReturnFalse_WhenAccessTokenCookieMissing() {
        // Arrange
        headers.add(HttpHeaders.COOKIE, "SESSIONID=123; OTHER=abc");

        // Act
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        // Assert
        assertFalse(result);
    }

    @Test
    void beforeHandshake_ShouldReturnFalse_WhenTokenInvalid() {
        // Arrange
        String token = "invalid.token";
        headers.add(HttpHeaders.COOKIE, "ACCESS_TOKEN=" + token);

        when(jwtUtilService.validateToken(token)).thenReturn(false);

        // Act
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        // Assert
        assertFalse(result);
    }

    @Test
    void beforeHandshake_ShouldReturnFalseAndUnauthorized_WhenUserNotFound() {
        // Arrange
        String token = "valid.token";
        String userId = "100";
        headers.add(HttpHeaders.COOKIE, "ACCESS_TOKEN=" + token);

        when(jwtUtilService.validateToken(token)).thenReturn(true);
        when(jwtUtilService.extractUserId(token)).thenReturn(userId);
        when(userService.getUserById(100L)).thenReturn(Optional.empty());

        // Act
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        // Assert
        assertFalse(result);
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void beforeHandshake_ShouldReturnTrueAndSetAttribute_WhenEverythingValid() {
        // Arrange
        String token = "valid.token";
        String userId = "100";
        String username = "testuser";

        headers.add(HttpHeaders.COOKIE, "SESSION=123; ACCESS_TOKEN=" + token + "; OTHER=xyz");

        when(jwtUtilService.validateToken(token)).thenReturn(true);
        when(jwtUtilService.extractUserId(token)).thenReturn(userId);

        UserDTO userDTO = new UserDTO(100L, username, new Timestamp(0L), new Timestamp(0L));
        when(userService.getUserById(100L)).thenReturn(Optional.of(userDTO));

        // Act
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        // Assert
        assertTrue(result);
        assertEquals(username, attributes.get("user"));
    }
}
