package de.thbingen.connect4.gaming.evt;

import de.thbingen.connect4.gaming.exception.LobbyNotFoundException;
import de.thbingen.connect4.gaming.model.dto.LobbyActorDTO;
import de.thbingen.connect4.gaming.ports.in.LobbyService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class LobbyEventListener {

    private final SimpMessagingTemplate messaging;
    private final LobbyService lobbyService;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) throws LobbyNotFoundException {
        if (event.getUser() == null) return;

        String usernameLeft = event.getUser().getName();
        lobbyService.removeUserFromAllLobbies(usernameLeft);
    }

    @EventListener
    public void onLobbyUpdate(LobbyUpdateEvent event) {
        for (LobbyActorDTO actor : event.lobby().actors()) {
            messaging.convertAndSendToUser(actor.username(), String.format("/topic/lobby/%s", event.lobby().id()), event);
        }
    }
}
