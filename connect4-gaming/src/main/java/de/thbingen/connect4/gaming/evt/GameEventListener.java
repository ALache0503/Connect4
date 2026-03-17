package de.thbingen.connect4.gaming.evt;

import de.thbingen.connect4.gaming.exception.LobbyException;
import de.thbingen.connect4.gaming.exception.LobbyNotFoundException;
import de.thbingen.connect4.gaming.model.dto.LobbyActorDTO;
import de.thbingen.connect4.gaming.model.dto.LobbyDTO;
import de.thbingen.connect4.gaming.model.entity.Lobby;
import de.thbingen.connect4.gaming.ports.in.GameService;
import de.thbingen.connect4.gaming.ports.in.LobbyDTOMapper;
import de.thbingen.connect4.gaming.ports.in.LobbyService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class GameEventListener {
    private final SimpMessagingTemplate messaging;
    private final LobbyService lobbyService;
    private final GameService gameService;
    private final LobbyDTOMapper lobbyDTOMapper;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        if (event.getUser() == null) return;

        String usernameLeft = event.getUser().getName();
        gameService.removeUserFromAllGames(usernameLeft);
    }

    @EventListener
    public void onGameTurn(GameTurnEvent event) throws LobbyException {
        Lobby lobby = lobbyService.getFromId(event.game().lobbyId()).orElseThrow(LobbyNotFoundException::new);
        LobbyDTO lobbyDto = lobbyDTOMapper.toDto(lobby);

        for (LobbyActorDTO actor : lobbyDto.actors()) {
            messaging.convertAndSendToUser(actor.username(), String.format("/topic/game/%s", lobbyDto.id()), event);
        }
    }
}
