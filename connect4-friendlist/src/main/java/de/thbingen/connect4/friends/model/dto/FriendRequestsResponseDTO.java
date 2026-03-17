package de.thbingen.connect4.friends.model.dto;

import java.util.List;

public record FriendRequestsResponseDTO(
        List<FriendRequestDTO> incoming,
        List<FriendRequestDTO> outgoing
) {
}
