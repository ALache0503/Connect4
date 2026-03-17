package de.thbingen.connect4.gaming.model.dto;

import de.thbingen.connect4.common.model.enums.LobbyState;

import java.util.Set;

public record LobbyDTO(String id, Set<LobbyActorDTO> actors, LobbyState state) {
}
