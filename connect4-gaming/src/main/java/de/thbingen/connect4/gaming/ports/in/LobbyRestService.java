package de.thbingen.connect4.gaming.ports.in;

import de.thbingen.connect4.common.model.dto.CreateLobbyResponse;
import de.thbingen.connect4.common.model.enums.LobbyActorRole;
import de.thbingen.connect4.common.model.enums.LobbyState;
import de.thbingen.connect4.gaming.exception.LobbyClosedException;
import de.thbingen.connect4.gaming.exception.LobbyException;
import de.thbingen.connect4.gaming.exception.LobbyFullException;
import de.thbingen.connect4.gaming.exception.LobbyNameGeneratedException;
import de.thbingen.connect4.gaming.exception.LobbyNotFoundException;
import de.thbingen.connect4.gaming.exception.LobbyStateChangeException;
import de.thbingen.connect4.gaming.model.dto.LobbyBotRemoveRequest;
import de.thbingen.connect4.gaming.model.dto.LobbyDTO;

import java.util.Optional;

public interface LobbyRestService {
    CreateLobbyResponse createLobby() throws LobbyNameGeneratedException;

    void joinLobby(String lobbyId, String name, LobbyActorRole role) throws LobbyClosedException, LobbyNotFoundException, LobbyFullException;

    void leaveLobby(String lobbyId, String name) throws LobbyNotFoundException;

    void setState(String lobbyId, String name, LobbyState newState) throws LobbyStateChangeException, LobbyNotFoundException;

    void setRole(String lobbyId, String name, LobbyActorRole role) throws LobbyException;

    Optional<LobbyDTO> getFromIdAuthenticated(String lobbyId, String name);

    Boolean isMember(String lobbyId, String username);

    void addBot(String lobbyId, String name) throws LobbyFullException, LobbyClosedException, LobbyNotFoundException;

    void removeBot(String lobbyId, LobbyBotRemoveRequest request) throws LobbyException;
}
