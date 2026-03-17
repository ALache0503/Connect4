package de.thbingen.connect4.gaming.ports.out;

import de.thbingen.connect4.common.model.dto.BotNameResultDTO;
import de.thbingen.connect4.common.model.dto.BotTurnRequestDTO;
import de.thbingen.connect4.common.model.dto.BotTurnResultDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "bot-service",
        url = "${services.bot-service.url}",
        path = "/api/v1/bot"
)
public interface BotRepository {
    @PostMapping("/turn")
    BotTurnResultDTO requestTurn(BotTurnRequestDTO dto);

    @GetMapping("/name")
    BotNameResultDTO requestName();
}
