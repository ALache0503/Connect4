package de.thbingen.connect4.gaming.service;

import de.thbingen.connect4.common.model.dto.BotNameResultDTO;
import de.thbingen.connect4.common.model.dto.BotTurnRequestDTO;
import de.thbingen.connect4.common.model.dto.BotTurnResultDTO;
import de.thbingen.connect4.common.model.dto.GameBoardDTO;
import de.thbingen.connect4.gaming.ports.in.BotService;
import de.thbingen.connect4.gaming.ports.out.BotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BotServiceImpl implements BotService {
    private final BotRepository botRepository;

    @Override
    public BotTurnResultDTO requestTurn(GameBoardDTO board) {
        return botRepository.requestTurn(new BotTurnRequestDTO(board));
    }

    @Override
    public BotNameResultDTO requestName() {
        return botRepository.requestName();
    }
}
