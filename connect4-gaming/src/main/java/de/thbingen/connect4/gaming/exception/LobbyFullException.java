package de.thbingen.connect4.gaming.exception;

public class LobbyFullException extends LobbyException {
    public LobbyFullException() {
        super("Lobby is full!");
    }
}
