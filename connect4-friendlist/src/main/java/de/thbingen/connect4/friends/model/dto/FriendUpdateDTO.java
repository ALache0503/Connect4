package de.thbingen.connect4.friends.model.dto;

import de.thbingen.connect4.friends.model.enums.FriendUpdateType;

public record FriendUpdateDTO(
        FriendUpdateType type,
        Long friendshipId,
        String fromUsername,
        String toUsername
) {
}
