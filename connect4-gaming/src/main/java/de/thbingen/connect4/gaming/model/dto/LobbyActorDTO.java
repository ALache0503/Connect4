package de.thbingen.connect4.gaming.model.dto;

import de.thbingen.connect4.common.model.enums.LobbyActorRole;

public record LobbyActorDTO(String username, LobbyActorRole role) {
}
