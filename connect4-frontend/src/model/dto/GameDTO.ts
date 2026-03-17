import type {GameBoardCellState} from "@/model/enum/GameBoarCellState.ts";

export interface GameDTO {
    lobbyId: string;
    state: 'RUNNING' | 'ENDED';
    currentTurn: string;
    player1: string;
    player2: string;
    board: {
        grid: GameBoardCellState[][];
    };
    turnResult: 'VALID' | 'DRAW' | 'WON';
    username: string;
}
