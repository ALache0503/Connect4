package de.thbingen.connect4.friends.adapters.in;

import de.thbingen.connect4.friends.exception.FriendshipException;
import de.thbingen.connect4.friends.model.dto.FriendDTO;
import de.thbingen.connect4.friends.model.dto.FriendRequestUserDTO;
import de.thbingen.connect4.friends.model.dto.FriendRequestsResponseDTO;
import de.thbingen.connect4.friends.model.dto.LobbyInviteDTO;
import de.thbingen.connect4.friends.ports.in.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/friends")
public class FriendshipRestController {

    private final FriendshipService friendshipService;

    @GetMapping
    public ResponseEntity<?> getFriends(Principal principal) {
        String username = resolveUsername(principal);
        List<FriendDTO> friends = friendshipService.getFriends(username);
        return ResponseEntity.ok(friends);
    }

    @GetMapping("/requests")
    public ResponseEntity<?> getRequests(Principal principal) {
        String username = resolveUsername(principal);
        FriendRequestsResponseDTO requests = friendshipService.getRequests(username);
        return ResponseEntity.ok(requests);
    }

    @PostMapping("/requests")
    public ResponseEntity<?> sendRequest(@RequestBody FriendRequestUserDTO dto, Principal principal) {
        String username = resolveUsername(principal);
        friendshipService.sendFriendRequest(username, dto.username());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/requests/{requestId}/accept")
    public ResponseEntity<?> acceptRequest(@PathVariable Long requestId, Principal principal) {
        String username = resolveUsername(principal);
        friendshipService.acceptFriendRequest(username, requestId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/requests/{requestId}/decline")
    public ResponseEntity<?> declineRequest(@PathVariable Long requestId, Principal principal) {
        String username = resolveUsername(principal);
        friendshipService.declineFriendRequest(username, requestId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{friendshipId}")
    public ResponseEntity<?> removeFriend(@PathVariable Long friendshipId, Principal principal) {
        String username = resolveUsername(principal);
        friendshipService.removeFriend(username, friendshipId);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(FriendshipException.class)
    public ResponseEntity<?> handleException(FriendshipException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @PostMapping("/invite/{lobbyId}")
    public ResponseEntity<?> inviteToLobby(@PathVariable String lobbyId,
                                           @RequestBody LobbyInviteDTO dto,
                                           Principal principal) {
        friendshipService.inviteToLobby(lobbyId, principal.getName(), dto.targetUsername());
        return ResponseEntity.ok().build();
    }

    private String resolveUsername(Principal principal) {
        return principal.getName();
    }
}