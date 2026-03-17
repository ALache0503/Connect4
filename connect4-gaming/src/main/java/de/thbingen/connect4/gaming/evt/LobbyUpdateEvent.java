package de.thbingen.connect4.gaming.evt;

import de.thbingen.connect4.gaming.model.dto.LobbyDTO;
import de.thbingen.connect4.gaming.model.enums.LobbyUpdateType;

public record LobbyUpdateEvent(LobbyDTO lobby, LobbyUpdateType type, String target) {
}
