package de.thbingen.connect4.chat.service;

import de.thbingen.connect4.chat.ports.out.LobbyRepository;
import de.thbingen.connect4.chat.ports.out.LobbyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LobbyServiceImpl implements LobbyService {

    private final LobbyRepository lobbyRepository;

    /*
    TODO
    Clean would be Event Driven Cache 'eventual consistency' from gaming-service
    but this would explode the time scope :(
     */
    @Override
    public boolean isMember(String lobbyId, String username) {
        return lobbyRepository.isMember(lobbyId, username);
    }
}
