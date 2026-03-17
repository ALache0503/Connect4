package de.thbingen.connect4.common.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class UserDetailsImplTest {

    private UserDetailsImpl userDetailsImpl;

    @BeforeEach
    void setUp() {
        userDetailsImpl = new UserDetailsImpl();
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUsernameIsNull() {
        // Arrange
        String username = null;

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> userDetailsImpl.loadUserByUsername(username));
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUsernameIsValid() {
        // Arrange
        String username = "testuser";

        // Act
        UserDetails result = userDetailsImpl.loadUserByUsername(username);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());

        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_USER")));
    }
}
