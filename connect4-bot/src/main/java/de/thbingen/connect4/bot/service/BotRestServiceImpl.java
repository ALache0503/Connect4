package de.thbingen.connect4.bot.service;

import de.thbingen.connect4.bot.ports.in.BotRestService;
import de.thbingen.connect4.bot.ports.in.BotService;
import de.thbingen.connect4.common.model.dto.BotNameResultDTO;
import de.thbingen.connect4.common.model.dto.BotTurnRequestDTO;
import de.thbingen.connect4.common.model.dto.BotTurnResultDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BotRestServiceImpl implements BotRestService {
    private final BotService botService;

    @Override
    public BotTurnResultDTO requestTurn(BotTurnRequestDTO requestDTO) {
        return botService.requestTurn(requestDTO);
    }

    @Override
    public BotNameResultDTO requestName() {
        return botService.requestName();
    }
}
