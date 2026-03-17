package de.thbingen.connect4.matchmaking.service;

import de.thbingen.connect4.common.model.dto.CreateLobbyResponse;
import de.thbingen.connect4.matchmaking.ports.in.LobbyService;
import de.thbingen.connect4.matchmaking.ports.out.LobbyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LobbyServiceImpl implements LobbyService {
    private final LobbyRepository lobbyRepository;

    @Override
    public CreateLobbyResponse createLobby() {
        return lobbyRepository.createLobby();
    }
}
