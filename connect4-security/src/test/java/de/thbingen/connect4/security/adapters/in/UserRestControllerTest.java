package de.thbingen.connect4.security.adapters.in;

import de.thbingen.connect4.common.filter.JWTFilter;
import de.thbingen.connect4.common.model.dto.UserDTO;
import de.thbingen.connect4.security.config.SecurityConfig;
import de.thbingen.connect4.security.model.entity.User;
import de.thbingen.connect4.security.ports.in.SecurityService;
import de.thbingen.connect4.security.ports.in.UserDTOMapper;
import de.thbingen.connect4.security.ports.in.UserSecurityService;
import de.thbingen.connect4.security.ports.out.UserSecurityRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserRestController.class, properties = {
        "jwt.expiration=3600",
        "services.security-service.url=http://localhost:9001" // DUMMY

})
@Import(SecurityConfig.class)
class UserRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserSecurityService userSecurityService;

    @MockitoBean
    private UserDTOMapper userDTOMapper;

    @MockitoBean
    private JWTFilter jwtFilter;
    @MockitoBean
    private UserDetailsService userDetailsService;
    @MockitoBean
    private SecurityService securityService;
    @MockitoBean
    private de.thbingen.connect4.common.ports.out.UserRepository commonUserRepository;
    @MockitoBean
    private UserSecurityRepository userSecurityRepository;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            invocation.getArgument(2, FilterChain.class).doFilter(
                    invocation.getArgument(0, ServletRequest.class),
                    invocation.getArgument(1, ServletResponse.class)
            );
            return null;
        }).when(jwtFilter).doFilter(any(), any(), any());
    }

    @Test
    void getUserById_ShouldReturnUserDTO_WhenFound() throws Exception {
        Long userId = 1L;
        User userMock = mock(User.class);
        UserDTO userDto = new UserDTO(userId, "testUser", new Timestamp(0L), new Timestamp(0L));

        when(userSecurityService.getUserById(userId)).thenReturn(Optional.of(userMock));
        when(userDTOMapper.toDto(userMock)).thenReturn(userDto);

        mockMvc.perform(get("/api/v1/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("testUser"));
    }

    @Test
    void getUserById_ShouldReturnBadRequest_WhenNotFound() throws Exception {
        Long userId = 99L;
        when(userSecurityService.getUserById(userId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/users/" + userId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUsername_ShouldReturnUsername_WhenFound() throws Exception {
        String username = "currentUser";
        User userMock = mock(User.class);
        UserDTO userDto = new UserDTO(1L, username, new Timestamp(0L), new Timestamp(0L));

        when(userSecurityService.getUserByUsername(username)).thenReturn(Optional.of(userMock));
        when(userDTOMapper.toDto(userMock)).thenReturn(userDto);

        mockMvc.perform(get("/api/v1/users/auth/username")
                        .with(csrf())
                        .with(user(username)))
                .andExpect(status().isOk())
                .andExpect(content().string(username));
    }

    @Test
    void getUsername_ShouldReturnBadRequest_WhenUserNotFoundInService() throws Exception {
        when(userSecurityService.getUserByUsername("unknownUser")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/users/auth/username").with(csrf()).with(user("unknownUser")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUsername_ShouldReturnBadRequest_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/users/auth/username"))
                .andExpect(status().isBadRequest());
    }
}
