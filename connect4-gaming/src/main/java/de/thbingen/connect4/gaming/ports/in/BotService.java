package de.thbingen.connect4.gaming.ports.in;

import de.thbingen.connect4.common.model.dto.BotNameResultDTO;
import de.thbingen.connect4.common.model.dto.BotTurnResultDTO;
import de.thbingen.connect4.common.model.dto.GameBoardDTO;

public interface BotService {
    BotTurnResultDTO requestTurn(GameBoardDTO board);

    BotNameResultDTO requestName();

}
