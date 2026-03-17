package de.thbingen.connect4.matchmaking.ports.in;

import de.thbingen.connect4.matchmaking.exception.MatchmakingSubscriptionException;

public interface MatchmakingRestControllerService {
    void register(String name) throws MatchmakingSubscriptionException;
}
