package de.thbingen.connect4.matchmaking;

import de.thbingen.connect4.matchmaking.ports.out.LobbyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class Connect4MatchmakingApplicationTests {

    @MockitoBean
    private LobbyRepository lobbyRepository;

    @Test
    void contextLoads() {
    }

}
