package de.thbingen.connect4.gaming.ports.out;

import de.thbingen.connect4.gaming.model.entity.Lobby;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class LobbyRepository {
    private final Map<String, Lobby> lobbies = new ConcurrentHashMap<>();

    public Collection<Lobby> findAll() {
        return lobbies.values();
    }

    public Lobby create(Lobby lobby) {
        lobbies.put(lobby.getId(), lobby);

        return lobby;
    }

    public Optional<Lobby> find(String id) {
        return Optional.ofNullable(lobbies.get(id));
    }

    public Optional<Lobby> findAuthenticated(String id, String username) {
        Optional<Lobby> optionalLobby = find(id);

        if (optionalLobby.isPresent()) {
            Lobby lobby = optionalLobby.get();

            if (lobby.getActors().stream().anyMatch(actor -> actor.getUsername().equals(username))) {
                return optionalLobby;
            }
        }

        return Optional.empty();
    }

    public void remove(String id) {
        lobbies.remove(id);
    }
}
