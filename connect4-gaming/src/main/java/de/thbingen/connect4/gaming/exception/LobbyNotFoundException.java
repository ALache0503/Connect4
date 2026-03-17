package de.thbingen.connect4.gaming.exception;

public class LobbyNotFoundException extends LobbyException {
    public LobbyNotFoundException() {
        super("Lobby Name not found");
    }
}
