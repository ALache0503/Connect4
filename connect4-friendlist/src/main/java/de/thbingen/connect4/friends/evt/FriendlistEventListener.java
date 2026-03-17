package de.thbingen.connect4.friends.evt;

import de.thbingen.connect4.friends.model.dto.FriendDTO;
import de.thbingen.connect4.friends.model.dto.FriendRequestDTO;
import de.thbingen.connect4.friends.model.dto.FriendUpdateDTO;
import de.thbingen.connect4.friends.model.entity.Friendship;
import de.thbingen.connect4.friends.ports.in.FriendshipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FriendlistEventListener {

    private final SimpMessagingTemplate messaging;
    private final FriendshipService friendshipService;

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        if (event.getUser() == null) return;

        String username = event.getUser().getName();
        friendshipService.userConnected(username);
        log.info("WebSocket connected: {}", username);
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        if (event.getUser() == null) return;

        String username = event.getUser().getName();
        friendshipService.userDisconnected(username);
        log.info("WebSocket disconnected: {}", username);
    }

    @EventListener
    public void onFriendRequest(FriendRequestEvent event) {
        FriendRequestDTO dto = event.friendRequestDTO();
        Friendship friendship = friendshipService.getFriendshipById(dto.id());

        String user1Username = friendship.getUsername1();
        String user2Username = friendship.getUsername2();

        FriendUpdateDTO update = new FriendUpdateDTO(
                event.type(),
                friendship.getId(),
                user1Username,
                user2Username
        );

        messaging.convertAndSendToUser(user1Username, "/topic/friends", update);
        messaging.convertAndSendToUser(user2Username, "/topic/friends", update);

        log.info("Sent friend update {} to {} and {}", event.type(), user1Username, user2Username);
    }

    @EventListener
    public void onOnlineStatusChange(OnlineStatusEvent event) {
        String username = event.username();

        List<FriendDTO> friends = friendshipService.getFriendsByUsername(username);

        if (friends.isEmpty()) {
            log.debug("No friends to notify for user {}", username);
            return;
        }

        FriendDTO statusUpdate = new FriendDTO(
                null,
                username,
                event.online()
        );

        for (FriendDTO friend : friends) {
            messaging.convertAndSendToUser(friend.username(), "/topic/friends/status", statusUpdate);
        }

        log.info("Sent online status update ({}) for {} to {} friends",
                event.online() ? "online" : "offline", username, friends.size());
    }

    @EventListener
    public void onLobbyInvite(LobbyInviteEvent event) {
        messaging.convertAndSendToUser(event.toUsername(), "/topic/lobby/invite", event);
    }
}