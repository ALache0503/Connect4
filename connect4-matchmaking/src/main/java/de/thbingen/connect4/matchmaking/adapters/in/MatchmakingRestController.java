package de.thbingen.connect4.matchmaking.adapters.in;

import de.thbingen.connect4.matchmaking.exception.MatchmakingSubscriptionException;
import de.thbingen.connect4.matchmaking.ports.in.MatchmakingRestControllerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/mm")
@RequiredArgsConstructor
public class MatchmakingRestController {

    private final MatchmakingRestControllerService matchmakingRestControllerService;

    @PostMapping("/register")
    public ResponseEntity<?> registerMM(Principal principal) throws MatchmakingSubscriptionException {
        matchmakingRestControllerService.register(principal.getName());

        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(MatchmakingSubscriptionException.class)
    public ResponseEntity<?> handleMMSubscriptionException(MatchmakingSubscriptionException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
