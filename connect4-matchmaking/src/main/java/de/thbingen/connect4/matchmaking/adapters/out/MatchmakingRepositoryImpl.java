package de.thbingen.connect4.matchmaking.adapters.out;

import de.thbingen.connect4.matchmaking.ports.out.MatchmakingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MatchmakingRepositoryImpl implements MatchmakingRepository {

    public static final String MM_QUEUE_TOPIC = "/user/topic/mm";
    private final SimpUserRegistry simpUserRegistry;

    @Override
    public List<String> findAllExceptWithUsername(String username) {
        List<String> users = new ArrayList<>();

        simpUserRegistry.getUsers().forEach(user -> {
            if (!user.getName().equals(username)) {
                user.getSessions().forEach(session -> {
                    session.getSubscriptions().forEach(subscription -> {
                        if (subscription.getDestination().equals(MM_QUEUE_TOPIC)) {
                            users.add(user.getName());
                        }
                    });
                });
            }
        });

        return users;
    }

    @Override
    public Set<String> findAll() {
        return simpUserRegistry.getUsers().stream().map(SimpUser::getName).collect(Collectors.toSet());
    }

    @Override
    public boolean isUserSubscribed(String username) {
        var user = simpUserRegistry.getUser(username);
        if (user == null) return false;

        return user.getSessions().stream()
                .flatMap(session -> session.getSubscriptions().stream())
                .anyMatch(subscription -> MM_QUEUE_TOPIC.equals(subscription.getDestination()));
    }

}
