package de.thbingen.connect4.friends.adapters.out;

import de.thbingen.connect4.friends.model.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataFriendshipRepository extends JpaRepository<Friendship, Long> {
    @Query("SELECT f FROM Friendship f WHERE (f.username1 = :username OR f.username2 = :username) AND f.status = 'ACCEPTED'")
    List<Friendship> findAcceptedFriendshipsByUsername(@Param("username") String username);

    @Query("SELECT f FROM Friendship f WHERE f.username2 = :username AND f.status = 'PENDING'")
    List<Friendship> findPendingIncomingRequests(@Param("username") String username);

    @Query("SELECT f FROM Friendship f WHERE f.username1 = :username AND f.status = 'PENDING'")
    List<Friendship> findPendingOutgoingRequests(@Param("username") String username);

    @Query("SELECT f FROM Friendship f WHERE (f.username1 = :user1 AND f.username2 = :user2) OR (f.username1 = :user2 AND f.username2 = :user1)")
    Optional<Friendship> findFriendshipBetweenUsers(@Param("user1") String user1, @Param("user2") String user2);

}