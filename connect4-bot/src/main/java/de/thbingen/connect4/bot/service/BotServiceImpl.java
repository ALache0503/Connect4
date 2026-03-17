package de.thbingen.connect4.bot.service;

import de.thbingen.connect4.bot.adapters.out.BotNameRepository;
import de.thbingen.connect4.bot.ports.in.BotService;
import de.thbingen.connect4.common.model.dto.BotNameResultDTO;
import de.thbingen.connect4.common.model.dto.BotTurnRequestDTO;
import de.thbingen.connect4.common.model.dto.BotTurnResultDTO;
import de.thbingen.connect4.common.model.dto.GameBoardDTO;
import de.thbingen.connect4.common.model.enums.GameBoardCellState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class BotServiceImpl implements BotService {

    @Override
    public BotTurnResultDTO requestTurn(BotTurnRequestDTO requestDTO) {
        return requestHardTurn(requestDTO);
    }

    @Override
    public BotNameResultDTO requestName() {
        Random random = new Random();
        List<String> botNames = BotNameRepository.BOT_NAMES;

        String botName = botNames.get(random.nextInt(botNames.size()));

        return new BotNameResultDTO(botName);
    }

    // Auto generated from KI
    private List<Integer> getValidColumns(GameBoardCellState[][] grid) {
        int cols = grid[0].length;
        List<Integer> valid = new ArrayList<>();
        for (int c = 0; c < cols; c++) {
            if (grid[0][c] == GameBoardCellState.EMPTY) valid.add(c);
        }
        return valid;
    }

    private void drop(GameBoardCellState[][] grid, int col, GameBoardCellState piece) {
        for (int r = grid.length - 1; r >= 0; r--) {
            if (grid[r][col] == GameBoardCellState.EMPTY) {
                grid[r][col] = piece;
                return;
            }
        }
    }

    private int scorePosition(GameBoardCellState[][] grid, GameBoardCellState me, GameBoardCellState opp) {
        int rows = grid.length;
        int cols = grid[0].length;
        int score = 0;

        // Center-Spalte bevorzugen (typisch sehr stark)
        int centerCol = cols / 2;
        int centerCount = 0;
        for (GameBoardCellState[] gameBoardCellStates : grid) {
            if (gameBoardCellStates[centerCol] == me) centerCount++;
        }
        score += centerCount * 3;

        // Windows of 4: horizontal, vertikal, diagonal
        // Horizontal
        for (GameBoardCellState[] gameBoardCellStates : grid) {
            for (int c = 0; c <= cols - 4; c++) {
                score += evaluateWindow(new GameBoardCellState[]{
                        gameBoardCellStates[c], gameBoardCellStates[c + 1], gameBoardCellStates[c + 2], gameBoardCellStates[c + 3]
                }, me, opp);
            }
        }
        // Vertikal
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r <= rows - 4; r++) {
                score += evaluateWindow(new GameBoardCellState[]{
                        grid[r][c], grid[r + 1][c], grid[r + 2][c], grid[r + 3][c]
                }, me, opp);
            }
        }
        // Diagonal (\)
        for (int r = 0; r <= rows - 4; r++) {
            for (int c = 0; c <= cols - 4; c++) {
                score += evaluateWindow(new GameBoardCellState[]{
                        grid[r][c], grid[r + 1][c + 1], grid[r + 2][c + 2], grid[r + 3][c + 3]
                }, me, opp);
            }
        }
        // Diagonal (/)
        for (int r = 3; r < rows; r++) {
            for (int c = 0; c <= cols - 4; c++) {
                score += evaluateWindow(new GameBoardCellState[]{
                        grid[r][c], grid[r - 1][c + 1], grid[r - 2][c + 2], grid[r - 3][c + 3]
                }, me, opp);
            }
        }

        return score;
    }

    private int evaluateWindow(GameBoardCellState[] window, GameBoardCellState me, GameBoardCellState opp) {
        int meCount = 0, oppCount = 0, emptyCount = 0;
        for (GameBoardCellState s : window) {
            if (s == me) meCount++;
            else if (s == opp) oppCount++;
            else emptyCount++;
        }

        // Typische Gewichtung: 4-in-a-row extrem hoch; 3+empty hoch; Gegner-3 blocken
        if (meCount == 4) return 1_000_000;
        if (meCount == 3 && emptyCount == 1) return 100;
        if (meCount == 2 && emptyCount == 2) return 10;

        if (oppCount == 3 && emptyCount == 1) return -120; // leicht stärker als eigener 3er, um Blocks zu priorisieren
        if (oppCount == 4) return -1_000_000;

        return 0;
    }

    private boolean isWinning(GameBoardCellState[][] grid, GameBoardCellState piece) {
        int rows = grid.length;
        int cols = grid[0].length;

        // Horizontal
        for (GameBoardCellState[] gameBoardCellStates : grid)
            for (int c = 0; c <= cols - 4; c++)
                if (gameBoardCellStates[c] == piece && gameBoardCellStates[c + 1] == piece && gameBoardCellStates[c + 2] == piece && gameBoardCellStates[c + 3] == piece)
                    return true;

        // Vertical
        for (int c = 0; c < cols; c++)
            for (int r = 0; r <= rows - 4; r++)
                if (grid[r][c] == piece && grid[r + 1][c] == piece && grid[r + 2][c] == piece && grid[r + 3][c] == piece)
                    return true;

        // Diagonal (\)
        for (int r = 0; r <= rows - 4; r++)
            for (int c = 0; c <= cols - 4; c++)
                if (grid[r][c] == piece && grid[r + 1][c + 1] == piece && grid[r + 2][c + 2] == piece && grid[r + 3][c + 3] == piece)
                    return true;

        // Diagonal (/)
        for (int r = 3; r < rows; r++)
            for (int c = 0; c <= cols - 4; c++)
                if (grid[r][c] == piece && grid[r - 1][c + 1] == piece && grid[r - 2][c + 2] == piece && grid[r - 3][c + 3] == piece)
                    return true;

        return false;
    }

    private GameBoardCellState[][] copyGrid(GameBoardCellState[][] grid) {
        GameBoardCellState[][] copy = new GameBoardCellState[grid.length][grid[0].length];
        for (int r = 0; r < grid.length; r++) {
            System.arraycopy(grid[r], 0, copy[r], 0, grid[r].length);
        }
        return copy;
    }

    private BotTurnResultDTO requestHardTurn(BotTurnRequestDTO requestDTO) {
        GameBoardDTO board = requestDTO.gameBoard();
        GameBoardCellState[][] grid = board.grid();

        GameBoardCellState me = GameBoardCellState.PLAYER_2;   // ggf. aus requestDTO ableiten
        GameBoardCellState opp = GameBoardCellState.PLAYER_1;

        List<Integer> validCols = getValidColumns(grid);
        if (validCols.isEmpty()) return new BotTurnResultDTO(0);

        int depth = 7; // 6-8 ist oft ein guter Start; abhängig von Performance
        int bestCol = validCols.getFirst();
        int bestScore = Integer.MIN_VALUE;

        // Move ordering: center zuerst (hilft Alpha-Beta typischerweise)
        validCols.sort(Comparator.comparingInt(c -> Math.abs(c - (grid[0].length / 2))));

        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (int col : validCols) {
            GameBoardCellState[][] child = copyGrid(grid);
            drop(child, col, me);

            int score = minimax(child, depth - 1, alpha, beta, false, me, opp);
            if (score > bestScore) {
                bestScore = score;
                bestCol = col;
            }
            alpha = bestScore;
        }

        return new BotTurnResultDTO(bestCol);
    }

    private int minimax(GameBoardCellState[][] grid, int depth, int alpha, int beta,
                        boolean maximizing, GameBoardCellState me, GameBoardCellState opp) {

        List<Integer> validCols = getValidColumns(grid);

        boolean terminal = isWinning(grid, me) || isWinning(grid, opp) || validCols.isEmpty();
        if (depth == 0 || terminal) {
            if (isWinning(grid, me)) return 1_000_000_000;
            if (isWinning(grid, opp)) return -1_000_000_000;
            if (validCols.isEmpty()) return 0;
            return scorePosition(grid, me, opp); // gleiche Heuristik wie beim Medium-Bot
        }

        // Ordering wieder center-first
        validCols.sort(Comparator.comparingInt(c -> Math.abs(c - (grid[0].length / 2))));

        if (maximizing) {
            int value = Integer.MIN_VALUE;
            for (int col : validCols) {
                GameBoardCellState[][] child = copyGrid(grid);
                drop(child, col, me);
                value = Math.max(value, minimax(child, depth - 1, alpha, beta, false, me, opp));
                alpha = Math.max(alpha, value);
                if (alpha >= beta) break; // Alpha-Beta Cutoff
            }
            return value;
        } else {
            int value = Integer.MAX_VALUE;
            for (int col : validCols) {
                GameBoardCellState[][] child = copyGrid(grid);
                drop(child, col, opp);
                value = Math.min(value, minimax(child, depth - 1, alpha, beta, true, me, opp));
                beta = Math.min(beta, value);
                if (alpha >= beta) break; // Alpha-Beta Cutoff
            }
            return value;
        }
    }

}
