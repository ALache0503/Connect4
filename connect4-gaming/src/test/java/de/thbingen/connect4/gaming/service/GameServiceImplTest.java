package de.thbingen.connect4.gaming.service;

import de.thbingen.connect4.common.model.dto.GameEventDto;
import de.thbingen.connect4.common.model.dto.GameTurnRequest;
import de.thbingen.connect4.common.model.enums.*;
import de.thbingen.connect4.gaming.evt.GameTurnEvent;
import de.thbingen.connect4.gaming.evt.LobbyUpdateEvent;
import de.thbingen.connect4.gaming.exception.GameNotFoundException;
import de.thbingen.connect4.gaming.exception.GameNotUserTurnException;
import de.thbingen.connect4.gaming.exception.GameNotValidTurnException;
import de.thbingen.connect4.gaming.exception.LobbyNotFoundException;
import de.thbingen.connect4.gaming.model.dto.GameDTO;
import de.thbingen.connect4.gaming.model.dto.LobbyActorDTO;
import de.thbingen.connect4.gaming.model.dto.LobbyDTO;
import de.thbingen.connect4.gaming.model.entity.Game;
import de.thbingen.connect4.gaming.model.entity.GameBoard;
import de.thbingen.connect4.gaming.model.entity.Lobby;
import de.thbingen.connect4.gaming.model.entity.LobbyActor;
import de.thbingen.connect4.gaming.model.enums.LobbyUpdateType;
import de.thbingen.connect4.gaming.ports.in.BotService;
import de.thbingen.connect4.gaming.ports.in.GameDTOMapper;
import de.thbingen.connect4.gaming.ports.in.LobbyDTOMapper;
import de.thbingen.connect4.gaming.ports.in.LobbyService;
import de.thbingen.connect4.gaming.ports.out.GameRepository;
import de.thbingen.connect4.gaming.ports.out.MessagingOutPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class GameServiceImplTest {

    @Mock
    GameRepository gameRepository;
    @Mock
    ApplicationEventPublisher publisher;
    @Mock
    GameDTOMapper gameDTOMapper;
    @Mock
    LobbyService lobbyService;
    @Mock
    LobbyDTOMapper lobbyDTOMapper;
    @Mock
    MessagingOutPort messagingOutPort;
    @Mock
    BotService botService;

    @InjectMocks
    GameServiceImpl gameService;

    @Captor
    ArgumentCaptor<GameTurnEvent> gameTurnEventCaptor;
    @Captor
    ArgumentCaptor<LobbyUpdateEvent> lobbyUpdateEventCaptor;
    @Captor
    ArgumentCaptor<GameEventDto> gameEventDtoCaptor;

    @Test
    void handleTurn_shouldThrowGameNotFoundException_whenRepoEmpty() {
        when(gameRepository.findAuthenticated("l1", "u1")).thenReturn(Optional.empty());

        assertThrows(GameNotFoundException.class,
                () -> gameService.handleTurn("l1", new GameTurnRequest(3), "u1"));

        verifyNoInteractions(publisher, gameDTOMapper, messagingOutPort, botService);
    }

    @Test
    void handleTurn_shouldThrowGameNotUserTurnException_whenNotUsersTurn() {
        Game game = mock(Game.class);
        LobbyActor currentTurn = mock(LobbyActor.class);

        when(gameRepository.findAuthenticated("l1", "u1")).thenReturn(Optional.of(game));
        when(game.getCurrentTurn()).thenReturn(currentTurn);
        when(currentTurn.getUsername()).thenReturn("someoneElse");

        assertThrows(GameNotUserTurnException.class,
                () -> gameService.handleTurn("l1", new GameTurnRequest(3), "u1"));

        verifyNoInteractions(publisher, gameDTOMapper, messagingOutPort, botService);
    }

    @Test
    void handleTurn_shouldThrowGameNotValidTurnException_whenDropDiscInvalid() {
        Game game = mock(Game.class);
        GameBoard board = mock(GameBoard.class);

        LobbyActor player1 = mock(LobbyActor.class);

        when(gameRepository.findAuthenticated("l1", "p1")).thenReturn(Optional.of(game));

        when(game.getCurrentTurn()).thenReturn(player1);
        when(player1.getUsername()).thenReturn("p1");

        when(game.getPlayer1()).thenReturn(player1);

        when(game.getBoard()).thenReturn(board);

        when(board.dropDisc(eq(2), any(GameBoardCellState.class)))
                .thenThrow(new IllegalStateException("full"));

        assertThrows(GameNotValidTurnException.class,
                () -> gameService.handleTurn("l1", new GameTurnRequest(2), "p1"));

        verify(game, never()).setCurrentTurn(any());
        verifyNoInteractions(publisher, gameDTOMapper, messagingOutPort, botService);
    }


    @Test
    void handleTurn_shouldSwitchTurn_andPublishGameTurnEvent_whenValidAndNoBot() throws Exception {
        Game game = mock(Game.class);
        GameBoard board = mock(GameBoard.class);

        LobbyActor player1 = mock(LobbyActor.class);
        LobbyActor player2 = mock(LobbyActor.class);

        when(player1.getUsername()).thenReturn("p1");

        when(player2.getRole()).thenReturn(LobbyActorRole.PLAYER);

        when(gameRepository.findAuthenticated("l1", "p1")).thenReturn(Optional.of(game));
        when(game.getCurrentTurn()).thenReturn(player1);
        when(game.getPlayer1()).thenReturn(player1);
        when(game.getPlayer2()).thenReturn(player2);
        when(game.getBoard()).thenReturn(board);

        when(board.dropDisc(eq(3), any(GameBoardCellState.class))).thenReturn(5);
        when(board.isWinningMove(5, 3)).thenReturn(false);
        when(board.isFull()).thenReturn(false);

        GameDTO dto = mock(GameDTO.class);
        when(gameDTOMapper.toDto(game)).thenReturn(dto);

        gameService.handleTurn("l1", new GameTurnRequest(3), "p1");

        verify(board).dropDisc(eq(3), any(GameBoardCellState.class));
        verify(board).isWinningMove(5, 3);
        verify(board).isFull();

        verify(game).setCurrentTurn(player2);
        verify(game, never()).setState(GameState.ENDED);
        verify(gameRepository, never()).remove("l1");
        verify(messagingOutPort, never()).sendGameEvent(any());
        verifyNoInteractions(botService);

        verify(publisher).publishEvent(gameTurnEventCaptor.capture());
        GameTurnEvent evt = gameTurnEventCaptor.getValue();
        assertEquals(dto, evt.game());
        assertEquals(GameTurnResult.VALID, evt.turnResult());
        assertEquals("p1", evt.username());
    }


    @Test
    void handleTurn_shouldEndGame_sendMessage_removeGame_andPublishEvent_whenWon() throws Exception {
        Game game = mock(Game.class);
        GameBoard board = mock(GameBoard.class);

        LobbyActor player1 = mock(LobbyActor.class);
        LobbyActor player2 = mock(LobbyActor.class);

        when(player1.getUsername()).thenReturn("p1");
        when(player2.getUsername()).thenReturn("p2");
        when(player1.getRole()).thenReturn(LobbyActorRole.PLAYER);
        when(player2.getRole()).thenReturn(LobbyActorRole.PLAYER);

        when(gameRepository.findAuthenticated("l1", "p1")).thenReturn(Optional.of(game));
        when(game.getCurrentTurn()).thenReturn(player1);
        when(game.getPlayer1()).thenReturn(player1);
        when(game.getPlayer2()).thenReturn(player2);
        when(game.getBoard()).thenReturn(board);

        when(board.dropDisc(eq(1), any(GameBoardCellState.class))).thenReturn(4);
        when(board.isWinningMove(4, 1)).thenReturn(true);

        GameDTO dto = mock(GameDTO.class);
        when(gameDTOMapper.toDto(game)).thenReturn(dto);

        gameService.handleTurn("l1", new GameTurnRequest(1), "p1");

        verify(game).setCurrentTurn(player2);
        verify(game).setState(GameState.ENDED);
        verify(gameRepository).remove("l1");

        verify(messagingOutPort).sendGameEvent(gameEventDtoCaptor.capture());
        GameEventDto msg = gameEventDtoCaptor.getValue();
        assertEquals(EventType.WIN, msg.getEventType());
        assertEquals("p1", msg.getWinnerUsername());
        assertEquals("p1", msg.getPlayer1Username());
        assertEquals("p2", msg.getPlayer2Username());

        verify(publisher).publishEvent(gameTurnEventCaptor.capture());
        GameTurnEvent evt = gameTurnEventCaptor.getValue();
        assertEquals(dto, evt.game());
        assertEquals(GameTurnResult.WON, evt.turnResult());
        assertEquals("p1", evt.username());
    }

    @Test
    void handleTurn_shouldEndGame_sendMessage_removeGame_andPublishEvent_whenDraw() throws Exception {
        Game game = mock(Game.class);
        GameBoard board = mock(GameBoard.class);

        LobbyActor player1 = mock(LobbyActor.class);
        LobbyActor player2 = mock(LobbyActor.class);

        when(player1.getUsername()).thenReturn("p1");
        when(player2.getUsername()).thenReturn("p2");
        when(player1.getRole()).thenReturn(LobbyActorRole.PLAYER);
        when(player2.getRole()).thenReturn(LobbyActorRole.PLAYER);

        when(gameRepository.findAuthenticated("l1", "p1")).thenReturn(Optional.of(game));
        when(game.getCurrentTurn()).thenReturn(player1);
        when(game.getPlayer1()).thenReturn(player1);
        when(game.getPlayer2()).thenReturn(player2);
        when(game.getBoard()).thenReturn(board);

        when(board.dropDisc(eq(0), any(GameBoardCellState.class))).thenReturn(5);
        when(board.isWinningMove(5, 0)).thenReturn(false);
        when(board.isFull()).thenReturn(true);

        GameDTO dto = mock(GameDTO.class);
        when(gameDTOMapper.toDto(game)).thenReturn(dto);

        gameService.handleTurn("l1", new GameTurnRequest(0), "p1");

        verify(game).setCurrentTurn(player2);
        verify(game).setState(GameState.ENDED);
        verify(gameRepository).remove("l1");

        verify(messagingOutPort).sendGameEvent(gameEventDtoCaptor.capture());
        GameEventDto msg = gameEventDtoCaptor.getValue();
        assertEquals(EventType.DRAW, msg.getEventType());
        assertNull(msg.getWinnerUsername());
        assertEquals("p1", msg.getPlayer1Username());
        assertEquals("p2", msg.getPlayer2Username());

        verify(publisher).publishEvent(gameTurnEventCaptor.capture());
        GameTurnEvent evt = gameTurnEventCaptor.getValue();
        assertEquals(GameTurnResult.DRAW, evt.turnResult());
    }

    @Test
    void removeUserFromAllGames_shouldEndGameAndPublishWin_whenAPlayerLeaves() {
        Game game = mock(Game.class);
        Lobby lobby = mock(Lobby.class);

        LobbyActor left = mock(LobbyActor.class);
        LobbyActor other = mock(LobbyActor.class);

        when(left.getUsername()).thenReturn("leaver");
        when(left.getRole()).thenReturn(LobbyActorRole.PLAYER);

        when(other.getUsername()).thenReturn("winner");
        when(other.getRole()).thenReturn(LobbyActorRole.PLAYER);

        when(game.getLobby()).thenReturn(lobby);
        when(lobby.getActors()).thenReturn(new HashSet<>(Set.of(left, other)));

        when(game.getPlayer1()).thenReturn(left);
        when(game.getPlayer2()).thenReturn(other);

        when(gameRepository.findAll()).thenReturn(List.of(game));

        GameDTO dto = mock(GameDTO.class);
        when(gameDTOMapper.toDto(game)).thenReturn(dto);

        gameService.removeUserFromAllGames("leaver");

        verify(game).setState(GameState.ENDED);

        verify(messagingOutPort).sendGameEvent(gameEventDtoCaptor.capture());
        assertEquals(EventType.WIN, gameEventDtoCaptor.getValue().getEventType());
        assertEquals("winner", gameEventDtoCaptor.getValue().getWinnerUsername());

        verify(publisher).publishEvent(gameTurnEventCaptor.capture());
        assertEquals(GameTurnResult.WON, gameTurnEventCaptor.getValue().turnResult());
        assertEquals("winner", gameTurnEventCaptor.getValue().username());
    }

    @Test
    void removeUserFromAllGames_shouldDoNothing_whenUserNotPlayerInAnyGame() {
        Game game = mock(Game.class);
        Lobby lobby = mock(Lobby.class);

        LobbyActor spectator = mock(LobbyActor.class);
        when(spectator.getUsername()).thenReturn("leaver");
        when(spectator.getRole()).thenReturn(LobbyActorRole.SPECTATOR);

        when(game.getLobby()).thenReturn(lobby);
        when(lobby.getActors()).thenReturn(new HashSet<>(Set.of(spectator)));

        when(gameRepository.findAll()).thenReturn(List.of(game));

        gameService.removeUserFromAllGames("leaver");

        verify(game, never()).setState(any());
        verifyNoInteractions(messagingOutPort, publisher, gameDTOMapper);
    }

    @Test
    void createGame_shouldThrowLobbyNotFoundException_whenLobbyMissing() {
        when(lobbyService.getFromIdAuthenticated("l1", "u1")).thenReturn(Optional.empty());

        assertThrows(LobbyNotFoundException.class, () -> gameService.createGame("l1", "u1"));

        verifyNoInteractions(gameRepository, publisher, lobbyDTOMapper, gameDTOMapper, botService);
    }

    @Test
    void createGame_shouldCreateGame_andPublishLobbyUpdateEvent_whenHumanVsHuman() throws Exception {
        Lobby lobby = mock(Lobby.class);

        LobbyActor p1 = mock(LobbyActor.class);
        LobbyActor p2 = mock(LobbyActor.class);

        when(p1.getRole()).thenReturn(LobbyActorRole.PLAYER);
        when(p2.getRole()).thenReturn(LobbyActorRole.PLAYER);

        when(lobby.getActors()).thenReturn(new HashSet<>(Set.of(p1, p2)));

        when(lobbyService.getFromIdAuthenticated("l1", "u1")).thenReturn(Optional.of(lobby));

        Set<LobbyActorDTO> lobbyActorDTOS = new HashSet<>();
        lobbyActorDTOS.add(new LobbyActorDTO("u1", LobbyActorRole.PLAYER));
        lobbyActorDTOS.add(new LobbyActorDTO("u2", LobbyActorRole.PLAYER));

        LobbyDTO lobbyDto = new LobbyDTO("l1", lobbyActorDTOS, LobbyState.OPEN);
        when(lobbyDTOMapper.toDto(lobby)).thenReturn(lobbyDto);

        gameService.createGame("l1", "u1");

        verify(gameRepository).create(any(Game.class));
        verifyNoInteractions(botService);

        verify(publisher).publishEvent(lobbyUpdateEventCaptor.capture());
        LobbyUpdateEvent evt = lobbyUpdateEventCaptor.getValue();
        assertEquals(LobbyUpdateType.GAME_START, evt.type());
        assertEquals("u1", evt.target());
        assertEquals(lobbyDto, evt.lobby());
    }
}
