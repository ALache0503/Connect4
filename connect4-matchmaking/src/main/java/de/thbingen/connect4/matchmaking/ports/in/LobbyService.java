package de.thbingen.connect4.matchmaking.ports.in;

import de.thbingen.connect4.common.model.dto.CreateLobbyResponse;

public interface LobbyService {
    CreateLobbyResponse createLobby();
}
