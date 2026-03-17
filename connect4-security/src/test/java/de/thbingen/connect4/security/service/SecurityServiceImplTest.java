package de.thbingen.connect4.security.service;

import de.thbingen.connect4.common.exception.UsernameTakenException;
import de.thbingen.connect4.common.model.dto.AuthRequest;
import de.thbingen.connect4.common.model.dto.AuthResponse;
import de.thbingen.connect4.common.model.enums.JwtTokenType;
import de.thbingen.connect4.common.ports.in.JwtUtilService;
import de.thbingen.connect4.security.exception.LoginFailedException;
import de.thbingen.connect4.security.model.entity.User;
import de.thbingen.connect4.security.ports.in.JwtService;
import de.thbingen.connect4.security.ports.in.UserSecurityService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityServiceImplTest {

    @Mock
    private JwtUtilService jwtUtilService;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserSecurityService userSecurityService;

    @InjectMocks
    private SecurityServiceImpl securityService;

    @Test
    void register_ShouldCreateUserAndReturnTokens_WhenUsernameAvailable() {
        // Arrange
        AuthRequest request = new AuthRequest("newUser", "password");
        User savedUser = User.builder().id(1L).username("newUser").password("hashed").build();

        when(userSecurityService.usernameExists("newUser")).thenReturn(false);
        when(userSecurityService.createUser(any(User.class))).thenReturn(savedUser);
        when(jwtService.generate(1L, JwtTokenType.ACCESS)).thenReturn("access-token");
        when(jwtService.generate(1L, JwtTokenType.REFRESH)).thenReturn("refresh-token");

        // Act
        AuthResponse response = securityService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());

        verify(userSecurityService).createUser(argThat(u ->
                u.getUsername().equals("newUser") && !u.getPassword().equals("password")
        ));
    }

    @Test
    void register_ShouldThrowException_WhenUsernameTaken() {
        AuthRequest request = new AuthRequest("existing", "pw");
        when(userSecurityService.usernameExists("existing")).thenReturn(true);

        assertThrows(UsernameTakenException.class, () -> securityService.register(request));

        verify(userSecurityService, never()).createUser(any());
    }

    @Test
    void login_ShouldReturnTokens_WhenCredentialsValid() {
        // Arrange
        String rawPassword = "secretPassword";
        String encodedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt(4));
        User user = User.builder().id(1L).username("user").password(encodedPassword).build();

        when(userSecurityService.getUserByUsername("user")).thenReturn(Optional.of(user));
        when(jwtService.generate(1L, JwtTokenType.ACCESS)).thenReturn("acc");
        when(jwtService.generate(1L, JwtTokenType.REFRESH)).thenReturn("ref");

        // Act
        AuthResponse response = securityService.login(new AuthRequest("user", rawPassword));

        // Assert
        assertEquals("acc", response.getAccessToken());
    }

    @Test
    void login_ShouldThrowException_WhenUserNotFound() {
        when(userSecurityService.getUserByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(LoginFailedException.class, () ->
                securityService.login(new AuthRequest("unknown", "pw"))
        );
    }

    @Test
    void login_ShouldThrowException_WhenPasswordInvalid() {
        String encoded = BCrypt.hashpw("correct", BCrypt.gensalt(4));
        User user = User.builder().id(1L).username("user").password(encoded).build();

        when(userSecurityService.getUserByUsername("user")).thenReturn(Optional.of(user));

        assertThrows(LoginFailedException.class, () ->
                securityService.login(new AuthRequest("user", "wrong"))
        );
    }

    @Test
    void refresh_ShouldReturnNewTokens_WhenTokenValid() {
        // Arrange
        String oldRefreshToken = "old-refresh";
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("1");

        when(jwtUtilService.getClaims(oldRefreshToken)).thenReturn(claims);

        User user = User.builder().id(1L).build();
        when(userSecurityService.getUserById(1L)).thenReturn(Optional.of(user));

        when(jwtService.generate(1L, JwtTokenType.ACCESS)).thenReturn("new-access");
        when(jwtService.generate(1L, JwtTokenType.REFRESH)).thenReturn("new-refresh");

        // Act
        AuthResponse response = securityService.refresh(oldRefreshToken);

        // Assert
        assertEquals("new-access", response.getAccessToken());
        assertEquals("new-refresh", response.getRefreshToken());
    }

    @Test
    void refresh_ShouldThrowException_WhenUserNotFound() {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("99");

        when(jwtUtilService.getClaims("valid-token-but-deleted-user")).thenReturn(claims);
        when(userSecurityService.getUserById(99L)).thenReturn(Optional.empty());

        assertThrows(LoginFailedException.class, () ->
                securityService.refresh("valid-token-but-deleted-user")
        );
    }
}
