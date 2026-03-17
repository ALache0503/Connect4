package de.thbingen.connect4.common.exception;

public class UsernameTakenException extends RuntimeException {
    public UsernameTakenException() {
        super("Username existiert bereits");
    }
}
