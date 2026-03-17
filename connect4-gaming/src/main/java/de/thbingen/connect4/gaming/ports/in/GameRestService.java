package de.thbingen.connect4.gaming.ports.in;

import de.thbingen.connect4.common.model.dto.GameTurnRequest;
import de.thbingen.connect4.gaming.exception.GameException;
import de.thbingen.connect4.gaming.exception.LobbyNotFoundException;
import de.thbingen.connect4.gaming.model.dto.GameDTO;

import java.util.Optional;

public interface GameRestService {
    void handleTurn(String lobbyId, GameTurnRequest request, String name) throws GameException;

    void createGame(String lobbyId, String name) throws LobbyNotFoundException, GameException;

    Optional<GameDTO> getGame(String lobbyId, String name) throws GameException;
}
