package de.thbingen.connect4.chat.ports.out;

public interface LobbyService {
    boolean isMember(String lobbyId, String username);
}
