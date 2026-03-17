package de.thbingen.connect4.gaming.adapters.in;

import de.thbingen.connect4.common.model.dto.GameTurnRequest;
import de.thbingen.connect4.gaming.exception.GameException;
import de.thbingen.connect4.gaming.exception.GameNotFoundException;
import de.thbingen.connect4.gaming.exception.LobbyNotFoundException;
import de.thbingen.connect4.gaming.model.dto.GameDTO;
import de.thbingen.connect4.gaming.ports.in.GameRestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/game")
public class GameRestController {

    private final GameRestService gameRestService;

    @PostMapping("/turn/{lobbyId}")
    public ResponseEntity<?> doTurn(@PathVariable String lobbyId, @RequestBody GameTurnRequest request, Principal principal) throws GameException {
        gameRestService.handleTurn(lobbyId, request, principal.getName());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/create/{lobbyId}")
    public ResponseEntity<?> createGame(@PathVariable String lobbyId, Principal principal) throws LobbyNotFoundException, GameException {
        gameRestService.createGame(lobbyId, principal.getName());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{lobbyId}")
    public ResponseEntity<GameDTO> getGame(@PathVariable String lobbyId, Principal principal) throws GameException {
        GameDTO game = gameRestService.getGame(lobbyId, principal.getName()).orElseThrow(GameNotFoundException::new);

        return ResponseEntity.ok(game);
    }

    @ExceptionHandler(GameException.class)
    public ResponseEntity<?> handleGameException(GameException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
