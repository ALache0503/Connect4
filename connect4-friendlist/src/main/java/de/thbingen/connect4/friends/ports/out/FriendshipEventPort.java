package de.thbingen.connect4.friends.ports.out;

import de.thbingen.connect4.friends.model.dto.FriendRequestDTO;
import de.thbingen.connect4.friends.model.enums.FriendUpdateType;

public interface FriendshipEventPort {

    void publishFriendRequestEvent(FriendRequestDTO dto, FriendUpdateType type);

    void publishOnlineStatus(String username, boolean online);

    void publishLobbyInvite(String lobbyId, String fromUsername, String toUsername);
}
