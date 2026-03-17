package de.thbingen.connect4.matchmaking.adapters.in;

import de.thbingen.connect4.common.filter.JWTFilter;
import de.thbingen.connect4.common.ports.out.UserRepository;
import de.thbingen.connect4.matchmaking.config.SecurityConfig;
import de.thbingen.connect4.matchmaking.exception.MatchmakingSubscriptionException;
import de.thbingen.connect4.matchmaking.ports.in.LobbyService;
import de.thbingen.connect4.matchmaking.ports.in.MatchmakingService;
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

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MatchmakingRestController.class)
@Import(SecurityConfig.class)
class MatchmakingRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MatchmakingService matchmakingService;

    @MockitoBean
    private LobbyService lobbyService;

    @MockitoBean
    private JWTFilter jwtFilter;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private UserRepository userRepository;

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
    void registerMM_ShouldReturnOk_WhenSuccessful() throws Exception {
        doNothing().when(matchmakingService).register("player1");

        mockMvc.perform(post("/api/v1/mm/register")
                        .with(csrf())
                        .with(user("player1")))
                .andExpect(status().isOk());

        verify(matchmakingService).register("player1");
    }

    @Test
    void registerMM_ShouldReturnBadRequest_WhenNotSubscribed() throws Exception {
        doThrow(new MatchmakingSubscriptionException()).when(matchmakingService)
                .register("player1");

        mockMvc.perform(post("/api/v1/mm/register")
                        .with(csrf())
                        .with(user("player1")))
                .andExpect(status().isBadRequest());
    }
}
