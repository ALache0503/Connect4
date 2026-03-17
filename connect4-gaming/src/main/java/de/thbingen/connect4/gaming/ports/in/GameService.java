package de.thbingen.connect4.gaming.ports.in;

import de.thbingen.connect4.common.model.dto.GameTurnRequest;
import de.thbingen.connect4.gaming.exception.GameException;
import de.thbingen.connect4.gaming.exception.LobbyNotFoundException;
import de.thbingen.connect4.gaming.model.entity.Game;

import java.util.Optional;

public interface GameService {
    void handleTurn(String lobbyId, GameTurnRequest request, String username) throws GameException;

    void removeUserFromAllGames(String usernameLeft);

    Optional<Game> getGame(String lobbyId, String username) throws GameException;

    void createGame(String lobbyId, String name) throws LobbyNotFoundException, GameException;

    void removeGame(String id);
}
