package de.thbingen.connect4.gaming.adapters.in;

import de.thbingen.connect4.common.model.dto.CreateLobbyResponse;
import de.thbingen.connect4.common.model.enums.LobbyActorRole;
import de.thbingen.connect4.common.model.enums.LobbyState;
import de.thbingen.connect4.gaming.exception.LobbyException;
import de.thbingen.connect4.gaming.exception.LobbyNotFoundException;
import de.thbingen.connect4.gaming.model.dto.LobbyBotRemoveRequest;
import de.thbingen.connect4.gaming.model.dto.LobbyDTO;
import de.thbingen.connect4.gaming.ports.in.LobbyRestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lobby")
public class LobbyRestController {

    private final LobbyRestService lobbyRestService;

    @PostMapping("/create")
    public ResponseEntity<CreateLobbyResponse> createLobbyRoom() throws LobbyException {
        return ResponseEntity.ok((lobbyRestService.createLobby()));
    }

    @PostMapping("/join/{lobbyId}")
    public ResponseEntity<?> joinLobbyRoom(@PathVariable String lobbyId, @RequestBody LobbyActorRole role, Principal principal) throws LobbyException {
        lobbyRestService.joinLobby(lobbyId, principal.getName(), role);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/leave/{lobbyId}")
    public ResponseEntity<?> leaveLobbyRoom(@PathVariable String lobbyId, Principal principal) throws LobbyException {
        lobbyRestService.leaveLobby(lobbyId, principal.getName());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/addbot/{lobbyId}")
    public ResponseEntity<?> addBot(@PathVariable String lobbyId, Principal principal) throws LobbyException {
        lobbyRestService.addBot(lobbyId, principal.getName());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/removebot/{lobbyId}")
    public ResponseEntity<?> removeBot(@PathVariable String lobbyId, @RequestBody LobbyBotRemoveRequest request) throws LobbyException {
        lobbyRestService.removeBot(lobbyId, request);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/state/{lobbyId}")
    public ResponseEntity<?> setState(@PathVariable String lobbyId, @RequestBody LobbyState newState, Principal principal) throws LobbyException {
        lobbyRestService.setState(lobbyId, principal.getName(), newState);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/role/{lobbyId}")
    public ResponseEntity<?> setRole(@PathVariable String lobbyId, @RequestBody LobbyActorRole role, Principal principal) throws LobbyException {
        lobbyRestService.setRole(lobbyId, principal.getName(), role);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{lobbyId}")
    public ResponseEntity<LobbyDTO> getLobby(@PathVariable String lobbyId, Principal principal) throws LobbyException {
        LobbyDTO lobby = lobbyRestService.getFromIdAuthenticated(lobbyId, principal.getName()).orElseThrow(LobbyNotFoundException::new);

        return ResponseEntity.ok(lobby);
    }

    @GetMapping("/checkMember/{lobbyId}/{username}")
    public ResponseEntity<Boolean> checkMember(@PathVariable String lobbyId, @PathVariable String username) {
        return ResponseEntity.ok(lobbyRestService.isMember(lobbyId, username));
    }

    @ExceptionHandler(LobbyException.class)
    public ResponseEntity<String> handleException(LobbyException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
