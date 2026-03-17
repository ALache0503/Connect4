package de.thbingen.connect4.gaming.adapters.in;

import de.thbingen.connect4.common.filter.JWTFilter;
import de.thbingen.connect4.common.filter.JWTHandshakeInterceptor;
import de.thbingen.connect4.common.model.dto.GameBoardDTO;
import de.thbingen.connect4.common.model.dto.GameTurnRequest;
import de.thbingen.connect4.common.model.enums.GameState;
import de.thbingen.connect4.common.ports.out.UserRepository;
import de.thbingen.connect4.gaming.config.SecurityConfig;
import de.thbingen.connect4.gaming.exception.GameException;
import de.thbingen.connect4.gaming.model.dto.GameDTO;
import de.thbingen.connect4.gaming.model.entity.Game;
import de.thbingen.connect4.gaming.ports.in.GameDTOMapper;
import de.thbingen.connect4.gaming.ports.in.GameService;
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
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GameRestController.class)
@Import(SecurityConfig.class)
class GameRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JWTFilter jwtFilter;

    @MockitoBean
    private GameService gameService;

    @MockitoBean
    private GameDTOMapper gameDTOMapper;

    @MockitoBean
    private JWTHandshakeInterceptor jwtHandshakeInterceptor;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

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
    void doTurn_ShouldReturnOk_WhenMoveIsValid() throws Exception {
        String lobbyId = "lobby-123";
        GameTurnRequest request = new GameTurnRequest(4);

        doNothing().when(gameService)
                .handleTurn(eq(lobbyId), any(GameTurnRequest.class), eq("player1"));

        mockMvc.perform(post("/api/v1/game/turn/" + lobbyId)
                        .with(csrf())
                        .with(user("player1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gameService).handleTurn(eq(lobbyId), any(GameTurnRequest.class), eq("player1"));
    }

    @Test
    void doTurn_ShouldReturnBadRequest_WhenServiceThrowsGameException() throws Exception {
        String lobbyId = "lobby-123";
        GameTurnRequest request = new GameTurnRequest(4);

        doThrow(new GameException("Not your turn")).when(gameService)
                .handleTurn(eq(lobbyId), any(GameTurnRequest.class), eq("player1"));

        mockMvc.perform(post("/api/v1/game/turn/" + lobbyId)
                        .with(csrf())
                        .with(user("player1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Not your turn"));
    }

    @Test
    void createGame_ShouldReturnOk_WhenSuccessful() throws Exception {
        String lobbyId = "new-lobby";

        mockMvc.perform(post("/api/v1/game/create/" + lobbyId)
                        .with(csrf())
                        .with(user("host")))
                .andExpect(status().isOk());

        verify(gameService).createGame(lobbyId, "host");
    }

    @Test
    void getGame_ShouldReturnGameDTO_WhenFound() throws Exception {
        String lobbyId = "lobby-123";
        Game gameMock = mock(Game.class);
        GameBoardDTO gameBoardDTOMock = mock(GameBoardDTO.class);
        GameDTO gameDTO = new GameDTO(lobbyId, GameState.RUNNING, "player1", "player1", "player2", gameBoardDTOMock);

        when(gameService.getGame(lobbyId, "player1")).thenReturn(Optional.of(gameMock));
        when(gameDTOMapper.toDto(gameMock)).thenReturn(gameDTO);

        mockMvc.perform(get("/api/v1/game/" + lobbyId)
                        .with(csrf())
                        .with(user("player1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lobbyId").value(lobbyId))
                .andExpect(jsonPath("$.player1").value("player1"));
    }

    @Test
    void getGame_ShouldThrowException_WhenGameNotFound() throws Exception {
        String lobbyId = "unknown-lobby";
        when(gameService.getGame(lobbyId, "player1")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/game/" + lobbyId)
                        .with(csrf())
                        .with(user("player1")))
                .andExpect(status().isBadRequest());
    }
}
