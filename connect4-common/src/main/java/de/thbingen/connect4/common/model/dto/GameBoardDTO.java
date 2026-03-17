package de.thbingen.connect4.common.model.dto;

import de.thbingen.connect4.common.model.enums.GameBoardCellState;

public record GameBoardDTO(GameBoardCellState[][] grid) {
}
