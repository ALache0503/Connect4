package de.thbingen.connect4.gaming.adapters.in;

import de.thbingen.connect4.common.filter.JWTFilter;
import de.thbingen.connect4.common.filter.JWTHandshakeInterceptor;
import de.thbingen.connect4.common.model.dto.CreateLobbyResponse;
import de.thbingen.connect4.common.model.enums.LobbyActorRole;
import de.thbingen.connect4.common.model.enums.LobbyState;
import de.thbingen.connect4.common.ports.out.UserRepository;
import de.thbingen.connect4.gaming.config.SecurityConfig;
import de.thbingen.connect4.gaming.exception.LobbyClosedException;
import de.thbingen.connect4.gaming.model.dto.LobbyActorDTO;
import de.thbingen.connect4.gaming.model.dto.LobbyDTO;
import de.thbingen.connect4.gaming.model.entity.Lobby;
import de.thbingen.connect4.gaming.ports.in.LobbyDTOMapper;
import de.thbingen.connect4.gaming.ports.in.LobbyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LobbyRestController.class)
@Import(SecurityConfig.class)
class LobbyRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LobbyService lobbyService;

    @MockitoBean
    private LobbyDTOMapper lobbyDTOMapper;

    @MockitoBean
    private JWTFilter jwtFilter;

    @MockitoBean
    private JWTHandshakeInterceptor jwtHandshakeInterceptor;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() throws ServletException, IOException {
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtFilter).doFilter(any(), any(), any());
    }

    @Test
    void createLobby_ShouldReturnOk_WhenSuccessful() throws Exception {
        // Arrange
        CreateLobbyResponse response = new CreateLobbyResponse("lobby-123");
        when(lobbyService.createLobby()).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/lobby/create")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lobbyId").value("lobby-123"));

        verify(lobbyService).createLobby();
    }

    @Test
    void joinLobby_ShouldReturnOk_WhenSuccessful() throws Exception {
        String lobbyId = "lobby-123";
        LobbyActorRole role = LobbyActorRole.PLAYER;

        // Service gibt void zurück, also doNothing() (default bei Mocks, aber explizit ist besser)
        doNothing().when(lobbyService).joinLobby(eq(lobbyId), eq("player1"), eq(role));

        mockMvc.perform(post("/api/v1/lobby/join/" + lobbyId)
                        .with(csrf())
                        .with(user("player1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(role))) // Enum wird als JSON-String "PLAYER_1" gesendet
                .andExpect(status().isOk());

        verify(lobbyService).joinLobby(eq(lobbyId), eq("player1"), eq(role));
    }

    @Test
    void joinLobby_ShouldReturnBadRequest_WhenLobbyClosed() throws Exception {
        String lobbyId = "lobby-closed";
        LobbyActorRole role = LobbyActorRole.PLAYER;

        doThrow(new LobbyClosedException()).when(lobbyService)
                .joinLobby(eq(lobbyId), eq("player1"), eq(role));

        mockMvc.perform(post("/api/v1/lobby/join/" + lobbyId)
                        .with(csrf())
                        .with(user("player1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(role)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void leaveLobby_ShouldReturnOk_WhenSuccessful() throws Exception {
        String lobbyId = "lobby-123";

        doNothing().when(lobbyService).leaveLobby(lobbyId, "player1");

        mockMvc.perform(post("/api/v1/lobby/leave/" + lobbyId)
                        .with(csrf())
                        .with(user("player1")))
                .andExpect(status().isOk());

        verify(lobbyService).leaveLobby(lobbyId, "player1");
    }

    @Test
    void setState_ShouldReturnOk_WhenSuccessful() throws Exception {
        String lobbyId = "lobby-123";
        LobbyState newState = LobbyState.OPEN;

        doNothing().when(lobbyService).setState(eq(lobbyId), eq("host"), eq(newState));

        mockMvc.perform(patch("/api/v1/lobby/state/" + lobbyId)
                        .with(csrf())
                        .with(user("host"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newState)))
                .andExpect(status().isOk());

        verify(lobbyService).setState(eq(lobbyId), eq("host"), eq(newState));
    }

    @Test
    void setRole_ShouldReturnOk_WhenSuccessful() throws Exception {
        String lobbyId = "lobby-123";
        LobbyActorRole newRole = LobbyActorRole.SPECTATOR;

        doNothing().when(lobbyService).setRole(eq(lobbyId), eq("player1"), eq(newRole));

        mockMvc.perform(patch("/api/v1/lobby/role/" + lobbyId)
                        .with(csrf())
                        .with(user("player1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newRole)))
                .andExpect(status().isOk());

        verify(lobbyService).setRole(eq(lobbyId), eq("player1"), eq(newRole));
    }

    @Test
    void getLobby_ShouldReturnLobbyDTO_WhenFound() throws Exception {
        String lobbyId = "lobby-123";

        Lobby lobbyMock = mock(Lobby.class);
        LobbyDTO lobbyDTO = new LobbyDTO(lobbyId,
                Set.of(new LobbyActorDTO("player1", LobbyActorRole.PLAYER), new LobbyActorDTO("player2", LobbyActorRole.PLAYER)),
                LobbyState.OPEN
        );

        when(lobbyService.getFromIdAuthenticated(lobbyId, "player1")).thenReturn(Optional.of(lobbyMock));
        when(lobbyDTOMapper.toDto(lobbyMock)).thenReturn(lobbyDTO);

        mockMvc.perform(get("/api/v1/lobby/" + lobbyId)
                        .with(csrf())
                        .with(user("player1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(lobbyId))
                .andExpect(jsonPath("$.state").value("OPEN"));
    }

    @Test
    void getLobby_ShouldReturnBadRequest_WhenNotFound() throws Exception {
        String lobbyId = "unknown";

        when(lobbyService.getFromIdAuthenticated(lobbyId, "player1")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/lobby/" + lobbyId)
                        .with(csrf())
                        .with(user("player1")))
                .andExpect(status().isBadRequest())
                .andExpect(MvcResult::getResolvedException);
    }
}
