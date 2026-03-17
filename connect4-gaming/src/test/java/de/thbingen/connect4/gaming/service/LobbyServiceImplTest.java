package de.thbingen.connect4.gaming.service;

import de.thbingen.connect4.common.model.dto.BotNameResultDTO;
import de.thbingen.connect4.common.model.dto.CreateLobbyResponse;
import de.thbingen.connect4.common.model.enums.LobbyActorRole;
import de.thbingen.connect4.common.model.enums.LobbyState;
import de.thbingen.connect4.gaming.evt.LobbyUpdateEvent;
import de.thbingen.connect4.gaming.exception.*;
import de.thbingen.connect4.gaming.model.dto.LobbyActorDTO;
import de.thbingen.connect4.gaming.model.dto.LobbyDTO;
import de.thbingen.connect4.gaming.model.entity.Lobby;
import de.thbingen.connect4.gaming.model.entity.LobbyActor;
import de.thbingen.connect4.gaming.model.enums.LobbyUpdateType;
import de.thbingen.connect4.gaming.ports.in.BotService;
import de.thbingen.connect4.gaming.ports.in.GameService;
import de.thbingen.connect4.gaming.ports.in.LobbyDTOMapper;
import de.thbingen.connect4.gaming.ports.out.LobbyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class LobbyServiceImplTest {

    @Mock
    LobbyRepository lobbyRepository;
    @Mock
    ApplicationEventPublisher publisher;
    @Mock
    LobbyDTOMapper lobbyMapper;
    @Mock
    BotService botService;
    @Mock
    GameService gameService;

    @InjectMocks
    LobbyServiceImpl lobbyService;

    @Captor
    ArgumentCaptor<LobbyUpdateEvent> eventCaptor;

    @Test
    void createLobby_shouldReturnCreatedId() throws Exception {
        Lobby created = new Lobby("lobby-123");

        when(lobbyRepository.create(any(Lobby.class))).thenReturn(created);

        CreateLobbyResponse resp = lobbyService.createLobby();

        assertEquals("lobby-123", resp.lobbyId());
        verify(lobbyRepository, times(1)).create(any(Lobby.class));
        verifyNoInteractions(publisher, lobbyMapper, botService, gameService);
    }

    @Test
    void joinLobby_shouldThrowLobbyNotFoundException_whenMissing() {
        when(lobbyRepository.find("l1")).thenReturn(Optional.empty());

        assertThrows(LobbyNotFoundException.class,
                () -> lobbyService.joinLobby("l1", "u1", LobbyActorRole.PLAYER));

        verifyNoInteractions(publisher, lobbyMapper);
    }

    @Test
    void joinLobby_shouldThrowLobbyFullException_whenTwoPlayingActorsAlready() {
        Lobby lobby = new Lobby("l1");
        lobby.setState(LobbyState.OPEN);

        LobbyActor p1 = new LobbyActor("p1", LobbyActorRole.PLAYER);
        LobbyActor p2 = new LobbyActor("p2", LobbyActorRole.PLAYER_BOT);

        lobby.getActors().add(p1);
        lobby.getActors().add(p2);

        when(lobbyRepository.find("l1")).thenReturn(Optional.of(lobby));

        assertThrows(LobbyFullException.class,
                () -> lobbyService.joinLobby("l1", "p3", LobbyActorRole.PLAYER));

        verifyNoInteractions(publisher, lobbyMapper);
        assertEquals(LobbyState.OPEN, lobby.getState());
    }

    @Test
    void joinLobby_shouldThrowLobbyClosedException_whenLobbyNotOpenAndRolePlayer() {
        Lobby lobby = new Lobby("l1");
        lobby.setState(LobbyState.CLOSED);

        when(lobbyRepository.find("l1")).thenReturn(Optional.of(lobby));

        assertThrows(LobbyClosedException.class,
                () -> lobbyService.joinLobby("l1", "u1", LobbyActorRole.PLAYER));

        verifyNoInteractions(publisher, lobbyMapper);
    }

    @Test
    void joinLobby_shouldAddActor_publishJoinEvent_andCloseLobbyWhenFull() throws Exception {
        Lobby lobby = new Lobby("l1");
        lobby.setState(LobbyState.OPEN);

        LobbyActor p1 = new LobbyActor("p1", LobbyActorRole.PLAYER);

        lobby.getActors().add(p1);

        when(lobbyRepository.find("l1")).thenReturn(Optional.of(lobby));

        Set<LobbyActorDTO> lobbyActorDTOs = new HashSet<>();
        lobbyActorDTOs.add(new LobbyActorDTO("p1", LobbyActorRole.PLAYER));

        LobbyDTO dto = new LobbyDTO("l1", lobbyActorDTOs, LobbyState.OPEN);
        when(lobbyMapper.toDto(lobby)).thenReturn(dto);

        lobbyService.joinLobby("l1", "p2", LobbyActorRole.PLAYER);

        assertEquals(LobbyState.CLOSED, lobby.getState());
        assertTrue(lobby.getActors().stream().anyMatch(a -> "p2".equals(a.getUsername())
                && a.getRole() == LobbyActorRole.PLAYER));

        verify(publisher).publishEvent(eventCaptor.capture());
        LobbyUpdateEvent evt = eventCaptor.getValue();
        assertEquals(LobbyUpdateType.JOIN, evt.type());
        assertEquals("p2", evt.target());
        assertEquals(dto, evt.lobby());
    }

    @Test
    void leaveLobby_shouldPublishLeftEvent_whenActorRemoved() throws Exception {
        Lobby lobby = new Lobby("l1");
        lobby.setState(LobbyState.OPEN);

        LobbyActor u1 = new LobbyActor("u1", LobbyActorRole.SPECTATOR);

        lobby.getActors().add(u1);

        when(lobbyRepository.find("l1")).thenReturn(Optional.of(lobby));

        Set<LobbyActorDTO> lobbyActorDTOS = new HashSet<>();
        lobbyActorDTOS.add(new LobbyActorDTO("u1", LobbyActorRole.SPECTATOR));

        LobbyDTO dto = new LobbyDTO("l1", lobbyActorDTOS, LobbyState.OPEN);
        when(lobbyMapper.toDto(lobby)).thenReturn(dto);

        lobbyService.leaveLobby("l1", "u1");

        assertTrue(lobby.getActors().isEmpty());

        verify(publisher).publishEvent(eventCaptor.capture());
        LobbyUpdateEvent evt = eventCaptor.getValue();
        assertEquals(LobbyUpdateType.LEFT, evt.type());
        assertEquals("u1", evt.target());
        assertEquals(dto, evt.lobby());
    }

    @Test
    void leaveLobby_shouldNotPublishEvent_whenNoActorRemoved() throws Exception {
        Lobby lobby = new Lobby("l1");
        lobby.setState(LobbyState.OPEN);

        LobbyActor u2 = new LobbyActor("u2", LobbyActorRole.SPECTATOR);
        lobby.getActors().add(u2);

        when(lobbyRepository.find("l1")).thenReturn(Optional.of(lobby));

        lobbyService.leaveLobby("l1", "u1");

        verifyNoInteractions(publisher, lobbyMapper);
    }

    @Test
    void removeUserFromAllLobbies_shouldCallLeaveLobbyForEachLobby() throws Exception {
        Lobby l1 = new Lobby("l1");
        l1.setState(LobbyState.OPEN);
        Lobby l2 = new Lobby("l2");
        l2.setState(LobbyState.OPEN);

        when(lobbyRepository.findAll()).thenReturn(List.of(l1, l2));
        when(lobbyRepository.find("l1")).thenReturn(Optional.of(l1));
        when(lobbyRepository.find("l2")).thenReturn(Optional.of(l2));

        lobbyService.removeUserFromAllLobbies("u1");

        verify(lobbyRepository, times(1)).findAll();
        verify(lobbyRepository, times(1)).find("l1");
        verify(lobbyRepository, times(1)).find("l2");
    }

    @Test
    void cleanUpLobbies_shouldRemoveOldBotOnlyLobby_andRemoveGame() {
        Date old = new Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(6));

        Lobby botOnlyOld = new Lobby("l1");
        botOnlyOld.setState(LobbyState.OPEN);
        botOnlyOld.setCreatedAt(old);

        LobbyActor bot = new LobbyActor("bot", LobbyActorRole.PLAYER_BOT);

        botOnlyOld.getActors().add(bot);

        Lobby withReal = new Lobby("l2");
        withReal.setState(LobbyState.OPEN);
        withReal.setCreatedAt(old);

        LobbyActor p1 = new LobbyActor("p1", LobbyActorRole.PLAYER);
        withReal.getActors().add(p1);

        when(lobbyRepository.findAll()).thenReturn(List.of(botOnlyOld, withReal));

        lobbyService.cleanUpLobbies();

        verify(lobbyRepository, times(1)).remove("l1");
        verify(gameService, times(1)).removeGame("l1");

        verify(lobbyRepository, never()).remove("l2");
        verify(gameService, never()).removeGame("l2");
    }

    @Test
    void setState_shouldThrowLobbyStateChangeException_whenOpeningFullLobby() {
        Lobby lobby = new Lobby("l1");
        lobby.setState(LobbyState.CLOSED);

        LobbyActor p1 = new LobbyActor("p1", LobbyActorRole.PLAYER);
        LobbyActor p2 = new LobbyActor("p2", LobbyActorRole.PLAYER_BOT);

        lobby.getActors().add(p1);
        lobby.getActors().add(p2);

        when(lobbyRepository.findAuthenticated("l1", "p1")).thenReturn(Optional.of(lobby));

        assertThrows(LobbyStateChangeException.class,
                () -> lobbyService.setState("l1", "p1", LobbyState.OPEN));

        verifyNoInteractions(publisher, lobbyMapper);
        assertEquals(LobbyState.CLOSED, lobby.getState());
    }

    @Test
    void setState_shouldUpdateState_andPublishStateChangeEvent() throws Exception {
        Lobby lobby = new Lobby("l1");
        lobby.setState(LobbyState.OPEN);

        when(lobbyRepository.findAuthenticated("l1", "u1")).thenReturn(Optional.of(lobby));

        LobbyDTO dto = new LobbyDTO("l1", new HashSet<>(), LobbyState.OPEN);
        when(lobbyMapper.toDto(lobby)).thenReturn(dto);

        lobbyService.setState("l1", "u1", LobbyState.CLOSED);

        assertEquals(LobbyState.CLOSED, lobby.getState());

        verify(publisher).publishEvent(eventCaptor.capture());
        LobbyUpdateEvent evt = eventCaptor.getValue();
        assertEquals(LobbyUpdateType.STATE_CHANGE, evt.type());
        assertEquals("OPEN", evt.target());
        assertEquals(dto, evt.lobby());
    }

    @Test
    void setRole_shouldThrowLobbyException_whenPlayingSlotsFullAndNewRolePlayer() {
        Lobby lobby = new Lobby("l1");
        lobby.setState(LobbyState.OPEN);

        LobbyActor p1 = new LobbyActor("p1", LobbyActorRole.PLAYER);
        LobbyActor p2 = new LobbyActor("p2", LobbyActorRole.PLAYER_BOT);
        LobbyActor u3 = new LobbyActor("u3", LobbyActorRole.SPECTATOR);

        lobby.getActors().add(p1);
        lobby.getActors().add(p2);
        lobby.getActors().add(u3);

        when(lobbyRepository.findAuthenticated("l1", "u3")).thenReturn(Optional.of(lobby));

        assertThrows(LobbyException.class,
                () -> lobbyService.setRole("l1", "u3", LobbyActorRole.PLAYER));

        verifyNoInteractions(publisher, lobbyMapper);
    }

    @Test
    void setRole_shouldThrowLobbyException_whenActorNotPartOfLobby() {
        Lobby lobby = new Lobby("l1");
        lobby.setState(LobbyState.OPEN);

        LobbyActor other = new LobbyActor("someoneElse", LobbyActorRole.SPECTATOR);

        lobby.getActors().add(other);

        when(lobbyRepository.findAuthenticated("l1", "u1")).thenReturn(Optional.of(lobby));

        assertThrows(LobbyException.class,
                () -> lobbyService.setRole("l1", "u1", LobbyActorRole.SPECTATOR));

        verifyNoInteractions(publisher, lobbyMapper);
    }

    @Test
    void setRole_shouldUpdateRole_andPublishRoleChangeEvent() throws Exception {
        Lobby lobby = new Lobby("l1");
        lobby.setState(LobbyState.OPEN);

        LobbyActor a = new LobbyActor("u1", LobbyActorRole.SPECTATOR);

        lobby.getActors().add(a);

        when(lobbyRepository.findAuthenticated("l1", "u1")).thenReturn(Optional.of(lobby));

        Set<LobbyActorDTO> lobbyActorDTOS = new HashSet<>();
        lobbyActorDTOS.add(new LobbyActorDTO("u1", LobbyActorRole.SPECTATOR));

        LobbyDTO dto = new LobbyDTO("l1", lobbyActorDTOS, LobbyState.OPEN);
        when(lobbyMapper.toDto(lobby)).thenReturn(dto);

        lobbyService.setRole("l1", "u1", LobbyActorRole.PLAYER);

        assertEquals(LobbyActorRole.PLAYER, a.getRole());

        verify(publisher).publishEvent(eventCaptor.capture());
        LobbyUpdateEvent evt = eventCaptor.getValue();
        assertEquals(LobbyUpdateType.ROLE_CHANGE, evt.type());
        assertEquals("u1", evt.target());
        assertEquals(dto, evt.lobby());
    }

    @Test
    void isMember_shouldReturnTrue_whenAuthenticatedFindPresent() {
        when(lobbyRepository.findAuthenticated("l1", "u1")).thenReturn(Optional.of(new Lobby("x")));
        assertTrue(lobbyService.isMember("l1", "u1"));
    }

    @Test
    void isMember_shouldReturnFalse_whenAuthenticatedFindEmpty() {
        when(lobbyRepository.findAuthenticated("l1", "u1")).thenReturn(Optional.empty());
        assertFalse(lobbyService.isMember("l1", "u1"));
    }

    @Test
    void addBot_shouldRequestName_andJoinLobbyAsPlayerBot() throws Exception {
        when(botService.requestName()).thenReturn(new BotNameResultDTO("bot-1"));

        Lobby lobby = new Lobby("l1");
        lobby.setState(LobbyState.OPEN);

        when(lobbyRepository.find("l1")).thenReturn(Optional.of(lobby));

        LobbyDTO dto = new LobbyDTO("l1", new HashSet<>(), LobbyState.OPEN);
        when(lobbyMapper.toDto(lobby)).thenReturn(dto);

        lobbyService.addBot("l1", "ignored");

        verify(botService, times(1)).requestName();
        verify(publisher, times(1)).publishEvent(any(LobbyUpdateEvent.class));
    }

    @Test
    void removeBot_shouldDelegateToLeaveLobby() throws Exception {
        Lobby lobby = new Lobby("l1");
        lobby.setState(LobbyState.OPEN);

        LobbyActor bot = new LobbyActor("bot-1", LobbyActorRole.PLAYER_BOT);

        lobby.getActors().add(bot);

        when(lobbyRepository.find("l1")).thenReturn(Optional.of(lobby));

        Set<LobbyActorDTO> lobbyActorDTOS = new HashSet<>();
        lobbyActorDTOS.add(new LobbyActorDTO("bot-1", LobbyActorRole.PLAYER_BOT));
        LobbyDTO lobbyDTO = new LobbyDTO("l1", lobbyActorDTOS, LobbyState.OPEN);

        when(lobbyMapper.toDto(lobby)).thenReturn(lobbyDTO);

        lobbyService.removeBot("l1", "bot-1");

        verify(publisher, times(1)).publishEvent(any(LobbyUpdateEvent.class));
    }
}
