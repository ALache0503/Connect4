package de.thbingen.connect4.gaming.model.entity;

import de.thbingen.connect4.common.model.enums.LobbyActorRole;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LobbyActor {
    private String username;
    private LobbyActorRole role;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LobbyActor that = (LobbyActor) o;

        return username.equals(that.username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }
}
