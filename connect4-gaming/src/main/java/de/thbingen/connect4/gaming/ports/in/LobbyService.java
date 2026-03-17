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
import de.thbingen.connect4.gaming.model.entity.Lobby;

import java.util.Optional;

public interface LobbyService {
    CreateLobbyResponse createLobby() throws LobbyNameGeneratedException;

    void joinLobby(String lobbyId, String username, LobbyActorRole role) throws LobbyNotFoundException, LobbyClosedException, LobbyFullException;

    void removeUserFromAllLobbies(String username) throws LobbyNotFoundException;

    void leaveLobby(String lobbyId, String username) throws LobbyNotFoundException;

    Optional<Lobby> getFromId(String id);

    Optional<Lobby> getFromIdAuthenticated(String id, String username);

    void cleanUpLobbies();

    void setState(String lobbyId, String name, LobbyState newState) throws LobbyNotFoundException, LobbyStateChangeException;

    void setRole(String lobbyId, String name, LobbyActorRole role) throws LobbyException;

    Boolean isMember(String lobbyId, String username);

    void addBot(String lobbyId, String username) throws LobbyNotFoundException, LobbyFullException, LobbyClosedException;

    void removeBot(String lobbyId, String botName) throws LobbyNotFoundException;
}
