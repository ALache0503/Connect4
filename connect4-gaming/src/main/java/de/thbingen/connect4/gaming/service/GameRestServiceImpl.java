package de.thbingen.connect4.gaming.service;

import de.thbingen.connect4.common.model.dto.GameTurnRequest;
import de.thbingen.connect4.gaming.exception.GameException;
import de.thbingen.connect4.gaming.exception.LobbyNotFoundException;
import de.thbingen.connect4.gaming.model.dto.GameDTO;
import de.thbingen.connect4.gaming.ports.in.GameDTOMapper;
import de.thbingen.connect4.gaming.ports.in.GameRestService;
import de.thbingen.connect4.gaming.ports.in.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameRestServiceImpl implements GameRestService {

    private final GameService gameService;
    private final GameDTOMapper gameDTOMapper;

    @Override
    public void handleTurn(String lobbyId, GameTurnRequest request, String name) throws GameException {
        gameService.handleTurn(lobbyId, request, name);
    }

    @Override
    public void createGame(String lobbyId, String name) throws LobbyNotFoundException, GameException {
        gameService.createGame(lobbyId, name);
    }

    @Override
    public Optional<GameDTO> getGame(String lobbyId, String name) throws GameException {
        return gameService.getGame(lobbyId, name).map(gameDTOMapper::toDto);
    }
}
