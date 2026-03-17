package de.thbingen.connect4.friends.adapters.out;

import de.thbingen.connect4.friends.evt.FriendRequestEvent;
import de.thbingen.connect4.friends.evt.LobbyInviteEvent;
import de.thbingen.connect4.friends.evt.OnlineStatusEvent;
import de.thbingen.connect4.friends.model.dto.FriendRequestDTO;
import de.thbingen.connect4.friends.model.enums.FriendUpdateType;
import de.thbingen.connect4.friends.ports.out.FriendshipEventPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringFriendshipEventAdapter implements FriendshipEventPort {

    private final ApplicationEventPublisher publisher;

    @Override
    public void publishFriendRequestEvent(FriendRequestDTO dto, FriendUpdateType type) {
        publisher.publishEvent(new FriendRequestEvent(dto, type));
    }

    @Override
    public void publishOnlineStatus(String username, boolean online) {
        publisher.publishEvent(new OnlineStatusEvent(username, online));
    }

    @Override
    public void publishLobbyInvite(String lobbyId, String fromUsername, String toUsername) {
        publisher.publishEvent(new LobbyInviteEvent(lobbyId, fromUsername, toUsername));
    }
}
