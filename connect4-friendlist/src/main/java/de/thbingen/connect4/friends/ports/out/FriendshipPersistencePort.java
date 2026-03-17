package de.thbingen.connect4.friends.ports.out;

import de.thbingen.connect4.friends.model.entity.Friendship;

import java.util.List;
import java.util.Optional;

public interface FriendshipPersistencePort {

    Friendship save(Friendship friendship);

    void delete(Friendship friendship);

    Optional<Friendship> findById(Long id);

    Optional<Friendship> findFriendshipBetweenUsers(String user1, String user2);

    List<Friendship> findAcceptedFriendshipsByUsername(String username);

    List<Friendship> findPendingIncomingRequests(String username);

    List<Friendship> findPendingOutgoingRequests(String username);
}
