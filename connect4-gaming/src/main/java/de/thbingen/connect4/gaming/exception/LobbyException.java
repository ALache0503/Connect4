package de.thbingen.connect4.gaming.exception;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LobbyException extends Exception {
    public LobbyException(String msg) {
        super(msg);
    }
}
