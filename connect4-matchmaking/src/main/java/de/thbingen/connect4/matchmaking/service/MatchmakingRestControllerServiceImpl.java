package de.thbingen.connect4.matchmaking.service;

import de.thbingen.connect4.matchmaking.exception.MatchmakingSubscriptionException;
import de.thbingen.connect4.matchmaking.ports.in.MatchmakingRestControllerService;
import de.thbingen.connect4.matchmaking.ports.in.MatchmakingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchmakingRestControllerServiceImpl implements MatchmakingRestControllerService {
    private final MatchmakingService matchmakingService;

    @Override
    public void register(String name) throws MatchmakingSubscriptionException {
        matchmakingService.register(name);
    }
}
