package de.thbingen.connect4.gaming.model.entity;

import de.thbingen.connect4.common.model.enums.GameBoardCellState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameBoardTest {

    private GameBoard gameBoard;

    @BeforeEach
    void setUp() {
        gameBoard = new GameBoard();
    }

    @Test
    void constructor_ShouldInitializeEmptyBoard() {
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 7; c++) {
                assertEquals(GameBoardCellState.EMPTY, gameBoard.getCell(r, c));
            }
        }
    }


    @Test
    void dropDisc_ShouldPlaceDiscAtBottom() {
        int row = gameBoard.dropDisc(0, GameBoardCellState.PLAYER_1);

        assertEquals(5, row);
        assertEquals(GameBoardCellState.PLAYER_1, gameBoard.getCell(5, 0));
    }

    @Test
    void dropDisc_ShouldStackDiscs() {
        gameBoard.dropDisc(0, GameBoardCellState.PLAYER_1);
        int row = gameBoard.dropDisc(0, GameBoardCellState.PLAYER_2);

        assertEquals(4, row);
        assertEquals(GameBoardCellState.PLAYER_1, gameBoard.getCell(5, 0));
        assertEquals(GameBoardCellState.PLAYER_2, gameBoard.getCell(4, 0));
    }

    @Test
    void dropDisc_ShouldThrowException_WhenColumnFull() {
        for (int i = 0; i < 6; i++) {
            gameBoard.dropDisc(0, GameBoardCellState.PLAYER_1);
        }

        assertTrue(gameBoard.isColumnFull(0));

        assertThrows(IllegalStateException.class, () ->
                gameBoard.dropDisc(0, GameBoardCellState.PLAYER_2)
        );
    }

    @Test
    void isWinningMove_ShouldDetectHorizontalWin() {
        gameBoard.setCell(5, 0, GameBoardCellState.PLAYER_1);
        gameBoard.setCell(5, 1, GameBoardCellState.PLAYER_1);
        gameBoard.setCell(5, 2, GameBoardCellState.PLAYER_1);
        gameBoard.setCell(5, 3, GameBoardCellState.PLAYER_1);

        assertTrue(gameBoard.isWinningMove(5, 3));

        gameBoard.setCell(5, 3, GameBoardCellState.EMPTY);
        assertFalse(gameBoard.isWinningMove(5, 2));
    }

    @Test
    void isWinningMove_ShouldDetectVerticalWin() {
        gameBoard.setCell(5, 0, GameBoardCellState.PLAYER_1);
        gameBoard.setCell(4, 0, GameBoardCellState.PLAYER_1);
        gameBoard.setCell(3, 0, GameBoardCellState.PLAYER_1);
        gameBoard.setCell(2, 0, GameBoardCellState.PLAYER_1);

        assertTrue(gameBoard.isWinningMove(2, 0));
    }

    @Test
    void isWinningMove_ShouldDetectDiagonalWin_DownRight() {
        gameBoard.setCell(2, 2, GameBoardCellState.PLAYER_1);
        gameBoard.setCell(3, 3, GameBoardCellState.PLAYER_1);
        gameBoard.setCell(4, 4, GameBoardCellState.PLAYER_1);
        gameBoard.setCell(5, 5, GameBoardCellState.PLAYER_1);

        assertTrue(gameBoard.isWinningMove(2, 2));
        assertTrue(gameBoard.isWinningMove(5, 5));
    }

    @Test
    void isWinningMove_ShouldDetectDiagonalWin_UpRight() {
        gameBoard.setCell(5, 2, GameBoardCellState.PLAYER_1);
        gameBoard.setCell(4, 3, GameBoardCellState.PLAYER_1);
        gameBoard.setCell(3, 4, GameBoardCellState.PLAYER_1);
        gameBoard.setCell(2, 5, GameBoardCellState.PLAYER_1);

        assertTrue(gameBoard.isWinningMove(2, 5));
    }

    @Test
    void isWinningMove_ShouldReturnFalse_WhenEmpty() {
        assertFalse(gameBoard.isWinningMove(3, 3));
    }

    @Test
    void isWinningMove_ShouldNotCountMixedColors() {
        gameBoard.setCell(5, 0, GameBoardCellState.PLAYER_1);
        gameBoard.setCell(5, 1, GameBoardCellState.PLAYER_1);
        gameBoard.setCell(5, 2, GameBoardCellState.PLAYER_1);
        gameBoard.setCell(5, 3, GameBoardCellState.PLAYER_2);

        assertFalse(gameBoard.isWinningMove(5, 2));
    }

    @Test
    void getCell_ShouldThrowException_WhenOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class, () -> gameBoard.getCell(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> gameBoard.getCell(6, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> gameBoard.getCell(0, 7));
    }

    @Test
    void isFull_ShouldReturnTrue_WhenBoardIsCompletelyFull() {
        for (int c = 0; c < 7; c++) {
            for (int r = 0; r < 6; r++) {
                gameBoard.setCell(r, c, GameBoardCellState.PLAYER_1);
            }
        }

        assertTrue(gameBoard.isFull());
    }

    @Test
    void isFull_ShouldReturnFalse_WhenAtLeastOneSpaceOpen() {
        for (int c = 0; c < 7; c++) {
            for (int r = 0; r < 6; r++) {
                gameBoard.setCell(r, c, GameBoardCellState.PLAYER_1);
            }
        }

        gameBoard.setCell(0, 0, GameBoardCellState.EMPTY);

        assertFalse(gameBoard.isFull());
    }
}
