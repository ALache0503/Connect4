package de.thbingen.connect4.security.exception;

public class LoginFailedException extends RuntimeException {
    public LoginFailedException() {
        super("Username und Password stimmen nicht über ein");
    }
}
