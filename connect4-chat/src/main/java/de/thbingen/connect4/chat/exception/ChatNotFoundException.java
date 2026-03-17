package de.thbingen.connect4.chat.exception;

public class ChatNotFoundException extends Exception {
    public ChatNotFoundException() {
        super("Chat not Found");
    }
}
