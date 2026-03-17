package de.thbingen.connect4.gaming.exception;

public class GameNotFoundException extends GameException {
    public GameNotFoundException() {
        super("Game not found!");
    }
}
