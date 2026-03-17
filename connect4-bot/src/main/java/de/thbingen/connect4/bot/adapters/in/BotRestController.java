package de.thbingen.connect4.bot.adapters.in;

import de.thbingen.connect4.bot.ports.in.BotRestService;
import de.thbingen.connect4.common.model.dto.BotNameResultDTO;
import de.thbingen.connect4.common.model.dto.BotTurnRequestDTO;
import de.thbingen.connect4.common.model.dto.BotTurnResultDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bot")
public class BotRestController {
    private final BotRestService botRestService;

    @PostMapping("/turn")
    public ResponseEntity<BotTurnResultDTO> requestTurn(@RequestBody BotTurnRequestDTO requestDTO) {
        BotTurnResultDTO result = botRestService.requestTurn(requestDTO);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/name")
    public ResponseEntity<BotNameResultDTO> requestName() {
        BotNameResultDTO result = botRestService.requestName();

        return ResponseEntity.ok(result);
    }
}
