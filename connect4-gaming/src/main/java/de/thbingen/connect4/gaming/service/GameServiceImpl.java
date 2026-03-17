package de.thbingen.connect4.gaming.service;

import de.thbingen.connect4.common.model.dto.BotTurnResultDTO;
import de.thbingen.connect4.common.model.dto.GameEventDto;
import de.thbingen.connect4.common.model.dto.GameTurnRequest;
import de.thbingen.connect4.common.model.enums.*;
import de.thbingen.connect4.gaming.evt.GameTurnEvent;
import de.thbingen.connect4.gaming.evt.LobbyUpdateEvent;
import de.thbingen.connect4.gaming.exception.*;
import de.thbingen.connect4.gaming.model.dto.GameDTO;
import de.thbingen.connect4.gaming.model.entity.Game;
import de.thbingen.connect4.gaming.model.entity.GameBoard;
import de.thbingen.connect4.gaming.model.entity.Lobby;
import de.thbingen.connect4.gaming.model.entity.LobbyActor;
import de.thbingen.connect4.gaming.model.enums.LobbyUpdateType;
import de.thbingen.connect4.gaming.ports.in.*;
import de.thbingen.connect4.gaming.ports.out.GameRepository;
import de.thbingen.connect4.gaming.ports.out.MessagingOutPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final ApplicationEventPublisher publisher;
    private final GameDTOMapper gameDTOMapper;
    private final LobbyDTOMapper lobbyDTOMapper;
    private final MessagingOutPort messagingOutPort;
    private final BotService botService;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    @Lazy
    @Autowired
    private LobbyService lobbyService;

    // Update test
    @Override
    public void handleTurn(String lobbyId, GameTurnRequest request, String username) throws GameException {
        Optional<Game> optionalGame = gameRepository.findAuthenticated(lobbyId, username);

        if (optionalGame.isEmpty()) {
            throw new GameNotFoundException();
        }

        Game game = optionalGame.get();
        LobbyActor currentTurnActor = game.getCurrentTurn();

        if (!currentTurnActor.getUsername().equals(username)) {
            throw new GameNotUserTurnException();
        }

        GameBoardCellState state = game.getPlayer1().equals(currentTurnActor)
                ? GameBoardCellState.PLAYER_1
                : GameBoardCellState.PLAYER_2;

        GameTurnResult turnResult = doTurn(request, game.getBoard(), state);

        if (turnResult == GameTurnResult.INVALID) {
            throw new GameNotValidTurnException();
        }

        // switch turns
        LobbyActor nextTurnActor = currentTurnActor.equals(game.getPlayer1()) ? game.getPlayer2() : game.getPlayer1();
        game.setCurrentTurn(nextTurnActor);

        if (turnResult == GameTurnResult.WON || turnResult == GameTurnResult.DRAW) {
            game.setState(GameState.ENDED);

            sendGameEventMessage(game, turnResult, username);
        }

        GameDTO gameDto = gameDTOMapper.toDto(game);

        if (nextTurnActor.getRole() == LobbyActorRole.PLAYER_BOT && turnResult == GameTurnResult.VALID) {
            BotTurnResultDTO botTurnResultDTO = botService.requestTurn(gameDto.board());
            handleBotTurn(lobbyId, botTurnResultDTO, nextTurnActor.getUsername());
        }

        if (turnResult == GameTurnResult.WON || turnResult == GameTurnResult.DRAW) {
            gameRepository.remove(lobbyId);
        }

        publisher.publishEvent(new GameTurnEvent(gameDto, turnResult, username));
    }

    @Override
    public void removeUserFromAllGames(String usernameLeft) {
        for (Game game : gameRepository.findAll()) {
            Optional<LobbyActor> actorLeftOptional = game.getLobby().getActors().stream().filter(
                            actor -> actor.getUsername().equals(usernameLeft) &&
                                    actor.getRole().equals(LobbyActorRole.PLAYER))
                    .findFirst();

            if (actorLeftOptional.isPresent()) {
                String winningUser = game.getPlayer1().equals(actorLeftOptional.get()) ?
                        game.getPlayer2().getUsername() :
                        game.getPlayer1().getUsername();

                game.setState(GameState.ENDED);

                sendGameEventMessage(game, GameTurnResult.WON, winningUser);

                GameDTO gameDto = gameDTOMapper.toDto(game);

                publisher.publishEvent(new GameTurnEvent(gameDto, GameTurnResult.WON, winningUser));
            }
        }
    }

    @Override
    public Optional<Game> getGame(String lobbyId, String username) {
        return gameRepository.findAuthenticated(lobbyId, username);
    }

    @Override
    public void createGame(String lobbyId, String username) throws LobbyNotFoundException, NoSuchElementException {
        Lobby lobby = lobbyService.getFromIdAuthenticated(lobbyId, username).orElseThrow(LobbyNotFoundException::new);

        List<LobbyActor> players = lobby.getActors().stream()
                .filter(actor -> actor.getRole() == LobbyActorRole.PLAYER || actor.getRole() == LobbyActorRole.PLAYER_BOT)
                .toList();

        LobbyActor player1 = players.getFirst();
        LobbyActor player2 = players.getLast();

        Game game = new Game(lobby, player1, player2);

        gameRepository.create(game);

        if (game.getCurrentTurn().getRole() == LobbyActorRole.PLAYER_BOT) {
            GameDTO gameDTO = gameDTOMapper.toDto(game);
            BotTurnResultDTO botTurnResultDTO = botService.requestTurn(gameDTO.board());
            handleBotTurn(lobbyId, botTurnResultDTO, game.getCurrentTurn().getUsername());
        }

        publisher.publishEvent(new LobbyUpdateEvent(lobbyDTOMapper.toDto(lobby), LobbyUpdateType.GAME_START, username));
    }

    @Override
    public void removeGame(String id) {
        gameRepository.remove(id);
    }

    private void handleBotTurn(String lobbyId, BotTurnResultDTO botTurnResultDTO, String username) {
        scheduler.schedule(() -> {
            try {
                handleTurn(lobbyId, new GameTurnRequest(botTurnResultDTO.column()), username);
            } catch (GameException e) {
                throw new RuntimeException(e);
            }
        }, 50, TimeUnit.MILLISECONDS);
    }

    private GameTurnResult doTurn(GameTurnRequest request, GameBoard gameBoard, GameBoardCellState player) {
        try {
            int row = gameBoard.dropDisc(request.column(), player);

            if (gameBoard.isWinningMove(row, request.column())) {
                return GameTurnResult.WON;
            } else if (gameBoard.isFull()) {
                return GameTurnResult.DRAW;
            } else {
                return GameTurnResult.VALID;
            }
        } catch (IllegalStateException e) {
            return GameTurnResult.INVALID;
        }
    }

    private void sendGameEventMessage(Game game, GameTurnResult turnResult, String winningUsername) {
        if (game.getPlayer1().getRole() == LobbyActorRole.PLAYER && game.getPlayer2().getRole() == LobbyActorRole.PLAYER) {
            EventType eventType = (turnResult == GameTurnResult.WON) ? EventType.WIN : EventType.DRAW;
            String winnerUsername = (turnResult == GameTurnResult.WON) ? winningUsername : null;

            GameEventDto gameEventDto = new GameEventDto(
                    eventType,
                    winnerUsername,
                    game.getPlayer1().getUsername(),
                    game.getPlayer2().getUsername()
            );

            messagingOutPort.sendGameEvent(gameEventDto);
        }
    }
}
