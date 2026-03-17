package de.thbingen.connect4.matchmaking.service;

import de.thbingen.connect4.matchmaking.evt.MatchmakingMatchFoundEvent;
import de.thbingen.connect4.matchmaking.exception.MatchmakingSubscriptionException;
import de.thbingen.connect4.matchmaking.ports.in.MatchmakingService;
import de.thbingen.connect4.matchmaking.ports.out.MatchmakingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MatchmakingServiceImpl implements MatchmakingService {

    private final MatchmakingRepository matchmakingRepository;
    private final ApplicationEventPublisher publisher;

    @Override
    public void register(String username) throws MatchmakingSubscriptionException {
        if (!matchmakingRepository.isUserSubscribed(username)) {
            throw new MatchmakingSubscriptionException();
        }

        List<String> otherUsers = matchmakingRepository.findAllExceptWithUsername(username);

        if (!otherUsers.isEmpty()) {
            Random random = new Random();

            String enemy = otherUsers.get(random.nextInt(otherUsers.size()));

            if (enemy != null) {
                publisher.publishEvent(new MatchmakingMatchFoundEvent(username, enemy));
            }
        }
    }

    @Override
    public Set<String> getAllUsers() {
        return matchmakingRepository.findAll();
    }
}
