package de.thbingen.connect4.matchmaking.ports.in;

import de.thbingen.connect4.matchmaking.exception.MatchmakingSubscriptionException;

import java.util.Set;

public interface MatchmakingService {

    void register(String username) throws MatchmakingSubscriptionException;

    Set<String> getAllUsers();
}
