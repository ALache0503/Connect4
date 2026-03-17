package de.thbingen.connect4.common.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.WebSocketHandler;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JWTHandshakeHandlerTest {

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private WebSocketHandler wsHandler;

    @InjectMocks
    private JWTHandshakeHandler handshakeHandler;

    @Test
    void determineUser_ShouldReturnNull_WhenAttributesAreNull() {
        Principal result = handshakeHandler.determineUser(request, wsHandler, null);
        assertNull(result);
    }

    @Test
    void determineUser_ShouldReturnNull_WhenUserAttributeIsMissing() {
        Map<String, Object> attributes = new HashMap<>();

        Principal result = handshakeHandler.determineUser(request, wsHandler, attributes);

        assertNull(result);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void determineUser_ShouldReturnAuthentication_WhenUserAttributeIsPresent() {
        // Arrange
        String username = "testuser";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("user", username);

        UserDetails userDetails = new User(username, "pass", Collections.emptyList());
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        // Act
        Principal result = handshakeHandler.determineUser(request, wsHandler, attributes);

        // Assert
        assertNotNull(result);
        assertInstanceOf(UsernamePasswordAuthenticationToken.class, result);
        assertEquals(username, result.getName());
        assertEquals(userDetails, ((UsernamePasswordAuthenticationToken) result).getPrincipal());

        verify(userDetailsService).loadUserByUsername(username);
    }
}
