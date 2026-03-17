package de.thbingen.connect4.gaming.exception;

public class GameNotUserTurnException extends GameException {
    public GameNotUserTurnException() {
        super("Not User turn");
    }
}
