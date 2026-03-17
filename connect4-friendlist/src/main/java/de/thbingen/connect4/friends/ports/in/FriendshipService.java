package de.thbingen.connect4.friends.ports.in;

import de.thbingen.connect4.friends.model.dto.FriendDTO;
import de.thbingen.connect4.friends.model.dto.FriendRequestsResponseDTO;
import de.thbingen.connect4.friends.model.entity.Friendship;

import java.util.List;

public interface FriendshipService {

    void sendFriendRequest(String fromUsername, String toUsername);

    void acceptFriendRequest(String username, Long friendshipId);

    void declineFriendRequest(String username, Long friendshipId);

    List<FriendDTO> getFriends(String username);

    List<FriendDTO> getFriendsByUsername(String username);

    FriendRequestsResponseDTO getRequests(String username);

    void removeFriend(String username, Long friendshipId);

    void userConnected(String username);

    void userDisconnected(String username);

    boolean isUserOnline(String username);

    void inviteToLobby(String lobbyId, String fromUsername, String targetUsername);

    Friendship getFriendshipById(Long friendshipId);
}