package de.thbingen.connect4.friends.adapters.out;

import de.thbingen.connect4.friends.model.entity.Friendship;
import de.thbingen.connect4.friends.ports.out.FriendshipPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FriendshipPersistenceAdapter implements FriendshipPersistencePort {

    private final SpringDataFriendshipRepository repository;

    @Override
    public Friendship save(Friendship friendship) {
        return repository.save(friendship);
    }

    @Override
    public void delete(Friendship friendship) {
        repository.delete(friendship);
    }

    @Override
    public Optional<Friendship> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Friendship> findFriendshipBetweenUsers(String user1, String user2) {
        return repository.findFriendshipBetweenUsers(user1, user2);
    }

    @Override
    public List<Friendship> findAcceptedFriendshipsByUsername(String username) {
        return repository.findAcceptedFriendshipsByUsername(username);
    }

    @Override
    public List<Friendship> findPendingIncomingRequests(String username) {
        return repository.findPendingIncomingRequests(username);
    }

    @Override
    public List<Friendship> findPendingOutgoingRequests(String username) {
        return repository.findPendingOutgoingRequests(username);
    }
}
