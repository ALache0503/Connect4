package de.thbingen.connect4.gaming.service;

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
import de.thbingen.connect4.gaming.ports.in.LobbyDTOMapper;
import de.thbingen.connect4.gaming.ports.in.LobbyRestService;
import de.thbingen.connect4.gaming.ports.in.LobbyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LobbyRestServiceImpl implements LobbyRestService {
    private final LobbyService lobbyService;
    private final LobbyDTOMapper lobbyDTOMapper;

    @Override
    public CreateLobbyResponse createLobby() throws LobbyNameGeneratedException {
        return lobbyService.createLobby();
    }

    @Override
    public void joinLobby(String lobbyId, String name, LobbyActorRole role) throws LobbyClosedException, LobbyNotFoundException, LobbyFullException {
        lobbyService.joinLobby(lobbyId, name, role);
    }

    @Override
    public void leaveLobby(String lobbyId, String name) throws LobbyNotFoundException {
        lobbyService.leaveLobby(lobbyId, name);
    }

    @Override
    public void setState(String lobbyId, String name, LobbyState newState) throws LobbyStateChangeException, LobbyNotFoundException {
        lobbyService.setState(lobbyId, name, newState);
    }

    @Override
    public void setRole(String lobbyId, String name, LobbyActorRole role) throws LobbyException {
        lobbyService.setRole(lobbyId, name, role);
    }

    @Override
    public Optional<LobbyDTO> getFromIdAuthenticated(String lobbyId, String name) {
        return lobbyService.getFromIdAuthenticated(lobbyId, name).map(lobbyDTOMapper::toDto);
    }

    @Override
    public Boolean isMember(String lobbyId, String username) {
        return lobbyService.isMember(lobbyId, username);
    }

    @Override
    public void addBot(String lobbyId, String username) throws LobbyFullException, LobbyClosedException, LobbyNotFoundException {
        lobbyService.addBot(lobbyId, username);
    }

    @Override
    public void removeBot(String lobbyId, LobbyBotRemoveRequest request) throws LobbyException {
        lobbyService.removeBot(lobbyId, request.botName());
    }
}
