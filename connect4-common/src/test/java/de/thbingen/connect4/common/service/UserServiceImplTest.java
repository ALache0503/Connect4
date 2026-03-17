package de.thbingen.connect4.common.service;

import de.thbingen.connect4.common.model.dto.UserDTO;
import de.thbingen.connect4.common.ports.out.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getUserById_ShouldDelegateToRepository() {
        // Arrange
        Long id = 123L;
        UserDTO expectedUser = new UserDTO(id, "test", new Timestamp(0L), new Timestamp(0L));
        when(userRepository.getUserById(id)).thenReturn(Optional.of(expectedUser));

        // Act
        Optional<UserDTO> result = userService.getUserById(id);

        // Assert
        assertEquals(Optional.of(expectedUser), result);
        verify(userRepository).getUserById(id);
    }
}
