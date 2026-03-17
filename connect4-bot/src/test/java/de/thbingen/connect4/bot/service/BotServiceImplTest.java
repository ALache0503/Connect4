package de.thbingen.connect4.bot.service;

import de.thbingen.connect4.bot.adapters.out.BotNameRepository;
import de.thbingen.connect4.common.model.dto.BotNameResultDTO;
import de.thbingen.connect4.common.model.dto.BotTurnRequestDTO;
import de.thbingen.connect4.common.model.dto.BotTurnResultDTO;
import de.thbingen.connect4.common.model.dto.GameBoardDTO;
import de.thbingen.connect4.common.model.enums.GameBoardCellState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BotServiceImplTest {

    private final BotServiceImpl botService = new BotServiceImpl();

    private static GameBoardCellState E() {
        return GameBoardCellState.EMPTY;
    }

    private static GameBoardCellState P1() {
        return GameBoardCellState.PLAYER_1;
    }

    private static GameBoardCellState P2() {
        return GameBoardCellState.PLAYER_2;
    }

    @Test
    void requestName_shouldReturnNameFromRepositoryList() {
        BotNameResultDTO res = botService.requestName();

        assertNotNull(res);
        assertNotNull(res.username());
        assertFalse(res.username().isBlank());

        assertTrue(BotNameRepository.BOT_NAMES.contains(res.username()));
    }

    @Test
    void requestTurn_shouldReturnValidColumnIndex() {
        GameBoardCellState[][] grid = new GameBoardCellState[][]{
                {E(), E(), E(), E(), E(), E(), E()},
                {E(), E(), E(), E(), E(), E(), E()},
                {E(), E(), E(), E(), E(), E(), E()},
                {E(), E(), E(), E(), E(), E(), E()},
                {E(), E(), E(), E(), E(), E(), E()},
                {E(), E(), E(), E(), E(), E(), E()},
        };

        BotTurnResultDTO res = botService.requestTurn(new BotTurnRequestDTO(new GameBoardDTO(grid)));

        assertNotNull(res);
        assertTrue(res.column() >= 0 && res.column() < 7);
    }

    @Test
    void requestTurn_shouldPlayWinningMove_whenAvailableForPlayer2() {
        GameBoardCellState[][] grid = new GameBoardCellState[][]{
                {E(), E(), E(), E(), E(), E(), E()},
                {E(), E(), E(), E(), E(), E(), E()},
                {E(), E(), E(), E(), E(), E(), E()},
                {E(), E(), E(), E(), E(), E(), E()},
                {E(), E(), E(), E(), E(), E(), E()},
                {P2(), P2(), P2(), E(), E(), E(), E()},
        };

        BotTurnResultDTO res = botService.requestTurn(new BotTurnRequestDTO(new GameBoardDTO(grid)));

        assertEquals(3, res.column());
    }

    @Test
    void requestTurn_shouldBlockOpponentWinningMove_whenPlayer1ThreatensWin() {
        GameBoardCellState[][] grid = new GameBoardCellState[][]{
                {E(), E(), E(), E(), E(), E(), E()},
                {E(), E(), E(), E(), E(), E(), E()},
                {E(), E(), E(), E(), E(), E(), E()},
                {E(), E(), E(), E(), E(), E(), E()},
                {E(), E(), E(), E(), E(), E(), E()},
                {P1(), P1(), P1(), E(), E(), E(), E()},
        };

        BotTurnResultDTO res = botService.requestTurn(new BotTurnRequestDTO(new GameBoardDTO(grid)));

        assertEquals(3, res.column());
    }

    @Test
    void requestTurn_shouldNeverChooseFullColumn() {
        GameBoardCellState[][] grid = new GameBoardCellState[][]{
                {P1(), E(), E(), E(), E(), E(), E()},
                {P2(), E(), E(), E(), E(), E(), E()},
                {P1(), E(), E(), E(), E(), E(), E()},
                {P2(), E(), E(), E(), E(), E(), E()},
                {P1(), E(), E(), E(), E(), E(), E()},
                {P2(), E(), E(), E(), E(), E(), E()},
        };

        BotTurnResultDTO res = botService.requestTurn(new BotTurnRequestDTO(new GameBoardDTO(grid)));

        assertNotEquals(0, res.column());
    }
}
