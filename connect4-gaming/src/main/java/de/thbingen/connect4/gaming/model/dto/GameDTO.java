package de.thbingen.connect4.gaming.model.dto;

import de.thbingen.connect4.common.model.enums.GameState;

public record GameDTO(String lobbyId,
                      GameState state,
                      String currentTurn,
                      String player1,
                      String player2,
                      de.thbingen.connect4.common.model.dto.GameBoardDTO board) {
}
