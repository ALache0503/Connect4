package de.thbingen.connect4.common.filter;

import de.thbingen.connect4.common.model.dto.UserDTO;
import de.thbingen.connect4.common.ports.in.JwtUtilService;
import de.thbingen.connect4.common.ports.out.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JWTFilterTest {
    @Mock
    private JwtUtilService jwtUtilService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserService userService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JWTFilter jwtFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_ShouldContinueChain_WhenNoAuthHeader() throws ServletException, IOException {
        // Act
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtUtilService);
    }

    @Test
    void doFilterInternal_ShouldContinueChain_WhenHeaderDoesNotStartWithBearer() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "Basic 12345");

        // Act
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtUtilService);
    }

    @Test
    void doFilterInternal_ShouldNotAuthenticate_WhenTokenInvalid() throws ServletException, IOException {
        // Arrange
        String token = "invalidToken";
        request.addHeader("Authorization", "Bearer " + token);
        when(jwtUtilService.validateToken(token)).thenReturn(false);

        // Act
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ShouldAuthenticate_WhenTokenValidAndUserFound() throws ServletException, IOException {
        // Arrange
        String token = "validToken";
        String userId = "100";
        String username = "testuser";

        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtilService.validateToken(token)).thenReturn(true);
        when(jwtUtilService.extractUserId(token)).thenReturn(userId);

        UserDTO userDTO = new UserDTO(100L, username, new Timestamp(0L), new Timestamp(0L)); // Passe Constructor an deine DTO an
        when(userService.getUserById(100L)).thenReturn(Optional.of(userDTO));

        UserDetails userDetails = new User(username, "password", Collections.emptyList());
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        // Act
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth, "Authentication should be present in SecurityContext");
        assertEquals(username, auth.getName());
        assertEquals(userDetails, auth.getPrincipal());
    }

    @Test
    void doFilterInternal_ShouldNotAuthenticate_WhenUserNotFound() throws ServletException, IOException {
        // Arrange
        String token = "validToken";
        String userId = "999";

        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtilService.validateToken(token)).thenReturn(true);
        when(jwtUtilService.extractUserId(token)).thenReturn(userId);

        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        // Act
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(userDetailsService); // UserDetails sollte gar nicht geladen werden
    }
}
