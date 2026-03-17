package de.thbingen.connect4.friends.evt;

import de.thbingen.connect4.friends.model.dto.FriendRequestDTO;
import de.thbingen.connect4.friends.model.enums.FriendUpdateType;

public record FriendRequestEvent(
        FriendRequestDTO friendRequestDTO,
        FriendUpdateType type
) {
}
