package de.thbingen.connect4.friends.evt;

public record OnlineStatusEvent(
        String username,
        boolean online
) {
}
