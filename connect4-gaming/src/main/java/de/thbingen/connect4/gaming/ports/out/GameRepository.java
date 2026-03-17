package de.thbingen.connect4.gaming.ports.out;

import de.thbingen.connect4.gaming.model.entity.Game;
import de.thbingen.connect4.gaming.model.entity.Lobby;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class GameRepository {
    private final Map<String, Game> games = new ConcurrentHashMap<>();

    public Collection<Game> findAll() {
        return games.values();
    }

    public Game create(Game game) {
        games.put(game.getLobby().getId(), game);

        return game;
    }

    public Optional<Game> find(String id) {
        return Optional.ofNullable(games.get(id));
    }

    public Optional<Game> findAuthenticated(String id, String username) {
        Optional<Game> optionalGame = find(id);

        if (optionalGame.isPresent()) {
            Lobby lobby = optionalGame.get().getLobby();

            if (lobby.getActors().stream().anyMatch(actor -> actor.getUsername().equals(username))) {
                return optionalGame;
            }
        }

        return Optional.empty();
    }

    public void remove(String id) {
        games.remove(id);
    }
}
