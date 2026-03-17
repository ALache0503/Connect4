package de.thbingen.connect4.gaming.exception;

public class LobbyClosedException extends LobbyException {
    public LobbyClosedException() {
        super("Lobby closed");
    }
}
