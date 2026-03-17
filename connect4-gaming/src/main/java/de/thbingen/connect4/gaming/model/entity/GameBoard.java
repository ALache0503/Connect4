package de.thbingen.connect4.gaming.model.entity;

import de.thbingen.connect4.common.model.enums.GameBoardCellState;
import lombok.Data;

import java.util.Arrays;

@Data
public class GameBoard {
    private final int ROWS = 6;
    private final int COLUMNS = 7;

    private final GameBoardCellState[][] grid;

    public GameBoard() {
        this.grid = new GameBoardCellState[ROWS][COLUMNS];

        for (GameBoardCellState[] row : grid) {
            Arrays.fill(row, GameBoardCellState.EMPTY);
        }
    }

    public GameBoardCellState getCell(int row, int col) {
        checkBounds(row, col);

        return grid[row][col];
    }

    public void setCell(int row, int col, GameBoardCellState value) {
        checkBounds(row, col);

        grid[row][col] = value;
    }

    public boolean isColumnFull(int col) {
        return grid[0][col] != GameBoardCellState.EMPTY;
    }

    public int dropDisc(int col, GameBoardCellState player) {
        if (isColumnFull(col)) {
            throw new IllegalStateException("Column is full");
        }

        for (int row = ROWS - 1; row >= 0; row--) {
            if (grid[row][col] == GameBoardCellState.EMPTY) {
                grid[row][col] = player;

                return row;
            }
        }

        throw new IllegalStateException("Unexpected: column full");
    }

    public boolean isWinningMove(int row, int col) {
        GameBoardCellState player = grid[row][col];
        if (player == GameBoardCellState.EMPTY) {
            return false;
        }

        return countConnected(row, col, 0, 1, player)       // →
                || countConnected(row, col, 1, 0, player)   // ↓
                || countConnected(row, col, 1, 1, player)   // ↘
                || countConnected(row, col, 1, -1, player); // ↙
    }

    private boolean countConnected(
            int row,
            int col,
            int rowDelta,
            int colDelta,
            GameBoardCellState player
    ) {
        int count = 1;

        // forward
        count += countOneDirection(row, col, rowDelta, colDelta, player);

        // backwards
        count += countOneDirection(row, col, -rowDelta, -colDelta, player);

        return count >= 4;
    }

    private int countOneDirection(
            int row,
            int col,
            int rowDelta,
            int colDelta,
            GameBoardCellState player
    ) {
        int r = row + rowDelta;
        int c = col + colDelta;
        int count = 0;

        while (r >= 0 && r < ROWS && c >= 0 && c < COLUMNS
                && grid[r][c] == player) {
            count++;
            r += rowDelta;
            c += colDelta;
        }
        return count;
    }

    private void checkBounds(int row, int col) {
        if (row < 0 || row >= ROWS || col < 0 || col >= COLUMNS) {
            throw new IndexOutOfBoundsException("Invalid board position");
        }
    }

    public boolean isFull() {
        for (int i = 0; i < COLUMNS; i++) {
            if (!isColumnFull(i)) {
                return false;
            }
        }

        return true;
    }
}
