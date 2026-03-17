package de.thbingen.connect4.gaming.evt;

import de.thbingen.connect4.common.model.enums.GameTurnResult;
import de.thbingen.connect4.gaming.model.dto.GameDTO;

public record GameTurnEvent(GameDTO game, GameTurnResult turnResult, String username) {
}
