package de.thbingen.connect4.gaming.exception;

public class GameNotValidTurnException extends GameException {
    public GameNotValidTurnException() {
        super("Not a valid turn");
    }
}
