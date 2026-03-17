package de.thbingen.connect4.friends.evt;

public record LobbyInviteEvent(
        String lobbyId,
        String fromUsername,
        String toUsername
) {
}