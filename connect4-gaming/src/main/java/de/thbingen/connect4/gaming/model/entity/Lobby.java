package de.thbingen.connect4.gaming.model.entity;

import de.thbingen.connect4.common.model.enums.LobbyState;
import lombok.Data;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class Lobby {
    private final String id;
    private final Set<LobbyActor> actors = ConcurrentHashMap.newKeySet();
    private LobbyState state;
    private Date createdAt;

    public Lobby(String id) {
        this.id = id;
        this.state = LobbyState.OPEN;

        createdAt = Date.from(Instant.now());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Lobby lobby = (Lobby) o;

        return lobby.id.equals(id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
