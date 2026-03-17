import {GameBoardCellState} from "@/model/enum/GameBoarCellState.ts";

export const CellColorMap: Record<GameBoardCellState, string> = {
    [GameBoardCellState.PLAYER_1]: "RED",
    [GameBoardCellState.PLAYER_2]: "YELLOW",
    [GameBoardCellState.EMPTY]: "EMPTY"
};