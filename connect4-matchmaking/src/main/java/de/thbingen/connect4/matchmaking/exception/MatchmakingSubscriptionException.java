package de.thbingen.connect4.matchmaking.exception;

public class MatchmakingSubscriptionException extends Exception {
    public MatchmakingSubscriptionException() {
        super("User not subscribed to matchmaking Queue");
    }
}
