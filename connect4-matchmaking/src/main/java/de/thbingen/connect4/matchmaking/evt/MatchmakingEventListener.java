package de.thbingen.connect4.matchmaking.evt;


import de.thbingen.connect4.common.model.dto.CreateLobbyResponse;
import de.thbingen.connect4.matchmaking.ports.in.LobbyService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchmakingEventListener {
    public static final String TOPIC_MM = "/topic/mm";

    private final SimpMessagingTemplate messagingTemplate;
    private final LobbyService lobbyService;

    @EventListener
    public void onMatchFound(MatchmakingMatchFoundEvent event) {
        CreateLobbyResponse lobbyResponse = lobbyService.createLobby();

        messagingTemplate.convertAndSendToUser(event.user1(), TOPIC_MM, lobbyResponse);
        messagingTemplate.convertAndSendToUser(event.user2(), TOPIC_MM, lobbyResponse);
    }
}
