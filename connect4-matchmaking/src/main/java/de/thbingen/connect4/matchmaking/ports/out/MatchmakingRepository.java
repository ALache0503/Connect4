package de.thbingen.connect4.matchmaking.ports.out;

import java.util.List;
import java.util.Set;

public interface MatchmakingRepository {
    List<String> findAllExceptWithUsername(String username);

    Set<String> findAll();

    boolean isUserSubscribed(String username);
}
