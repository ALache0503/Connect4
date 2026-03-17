package de.thbingen.connect4.bot.ports.in;

import de.thbingen.connect4.common.model.dto.BotNameResultDTO;
import de.thbingen.connect4.common.model.dto.BotTurnRequestDTO;
import de.thbingen.connect4.common.model.dto.BotTurnResultDTO;

public interface BotService {
    BotTurnResultDTO requestTurn(BotTurnRequestDTO requestDTO);

    BotNameResultDTO requestName();
}
