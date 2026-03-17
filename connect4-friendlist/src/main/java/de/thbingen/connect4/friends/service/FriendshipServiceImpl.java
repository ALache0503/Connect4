package de.thbingen.connect4.friends.service;

import de.thbingen.connect4.friends.exception.FriendshipException;
import de.thbingen.connect4.friends.exception.FriendshipNotFoundException;
import de.thbingen.connect4.friends.model.dto.FriendDTO;
import de.thbingen.connect4.friends.model.dto.FriendRequestDTO;
import de.thbingen.connect4.friends.model.dto.FriendRequestsResponseDTO;
import de.thbingen.connect4.friends.model.entity.Friendship;
import de.thbingen.connect4.friends.model.enums.FriendUpdateType;
import de.thbingen.connect4.friends.model.enums.FriendshipStatus;
import de.thbingen.connect4.friends.ports.in.FriendshipService;
import de.thbingen.connect4.friends.ports.out.FriendshipEventPort;
import de.thbingen.connect4.friends.ports.out.FriendshipPersistencePort;
import de.thbingen.connect4.friends.ports.out.OnlineUserPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendshipServiceImpl implements FriendshipService {

    private final FriendshipPersistencePort friendshipPort;
    private final FriendshipEventPort eventPort;
    private final OnlineUserPort onlineUserPort;

    @Override
    @Transactional
    public void sendFriendRequest(String fromUsername, String toUsername) {
        if (fromUsername == null || fromUsername.isBlank() || toUsername == null || toUsername.isBlank()) {
            throw new FriendshipException("Benutzername fehlt");
        }

        if (fromUsername.equalsIgnoreCase(toUsername)) {
            throw new FriendshipException("Du kannst dir selbst keine Freundschaftsanfrage senden");
        }

        Optional<Friendship> existing = friendshipPort.findFriendshipBetweenUsers(fromUsername, toUsername);
        if (existing.isPresent()) {
            Friendship f = existing.get();
            if (f.getStatus() == FriendshipStatus.ACCEPTED) {
                throw new FriendshipException("Ihr seid bereits Freunde");
            } else if (f.getStatus() == FriendshipStatus.PENDING) {
                throw new FriendshipException("Es existiert bereits eine ausstehende Anfrage");
            }
        }

        Friendship friendship = new Friendship();
        friendship.setUsername1(fromUsername);
        friendship.setUsername2(toUsername);
        friendship.setStatus(FriendshipStatus.PENDING);

        Friendship saved;
        try {
            saved = friendshipPort.save(friendship);
        } catch (DataIntegrityViolationException ex) {
            throw new FriendshipNotFoundException("Benutzer nicht gefunden");
        }

        FriendRequestDTO dto = new FriendRequestDTO(
                saved.getId(),
                toUsername
        );

        eventPort.publishFriendRequestEvent(dto, FriendUpdateType.REQUEST_RECEIVED);
        log.info("Friend request sent from user {} to user {}", fromUsername, toUsername);

    }

    @Override
    @Transactional
    public void acceptFriendRequest(String username, Long friendshipId) {
        Friendship friendship = friendshipPort.findById(friendshipId)
                .orElseThrow(() -> new FriendshipNotFoundException("Anfrage nicht gefunden"));

        if (!friendship.getUsername2().equals(username)) {
            throw new FriendshipException("Du bist nicht berechtigt, diese Anfrage anzunehmen");
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new FriendshipException("Diese Anfrage ist nicht mehr ausstehend");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        Friendship saved = friendshipPort.save(friendship);

        FriendRequestDTO dto = new FriendRequestDTO(
                saved.getId(),
                friendship.getUsername1()
        );

        eventPort.publishFriendRequestEvent(dto, FriendUpdateType.REQUEST_ACCEPTED);
        log.info("Friend request {} accepted by user {}", friendshipId, username);

    }

    @Override
    @Transactional
    public void declineFriendRequest(String username, Long friendshipId) {
        Friendship friendship = friendshipPort.findById(friendshipId)
                .orElseThrow(() -> new FriendshipNotFoundException("Anfrage nicht gefunden"));

        if (!friendship.getUsername2().equals(username)) {
            throw new FriendshipException("Du bist nicht berechtigt, diese Anfrage abzulehnen");
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new FriendshipException("Diese Anfrage ist nicht mehr ausstehend");
        }

        friendship.setStatus(FriendshipStatus.DECLINED);
        Friendship saved = friendshipPort.save(friendship);

        FriendRequestDTO dto = new FriendRequestDTO(
                saved.getId(),
                friendship.getUsername1()
        );

        eventPort.publishFriendRequestEvent(dto, FriendUpdateType.REQUEST_DECLINED);
        log.info("Friend request {} declined by user {}", friendshipId, username);

    }

    @Override
    public List<FriendDTO> getFriends(String username) {
        List<Friendship> friendships = friendshipPort.findAcceptedFriendshipsByUsername(username);
        List<FriendDTO> friends = new ArrayList<>();

        for (Friendship f : friendships) {
            String friendUsername = f.getUsername1().equals(username) ? f.getUsername2() : f.getUsername1();
            boolean online = isUserOnline(friendUsername);
            friends.add(new FriendDTO(null, friendUsername, online));
        }

        return friends;
    }

    @Override
    public List<FriendDTO> getFriendsByUsername(String username) {
        return getFriends(username);
    }

    @Override
    public FriendRequestsResponseDTO getRequests(String username) {
        List<Friendship> incoming = friendshipPort.findPendingIncomingRequests(username);
        List<Friendship> outgoing = friendshipPort.findPendingOutgoingRequests(username);

        List<FriendRequestDTO> incomingDtos = incoming.stream()
                .map(f -> new FriendRequestDTO(f.getId(), f.getUsername1()))
                .toList();

        List<FriendRequestDTO> outgoingDtos = outgoing.stream()
                .map(f -> new FriendRequestDTO(f.getId(), f.getUsername2()))
                .toList();

        return new FriendRequestsResponseDTO(incomingDtos, outgoingDtos);
    }

    @Override
    @Transactional
    public void removeFriend(String username, Long friendshipId) {
        Friendship friendship = friendshipPort.findById(friendshipId)
                .orElseThrow(() -> new FriendshipNotFoundException("Freundschaft nicht gefunden"));

        if (!friendship.getUsername1().equals(username) && !friendship.getUsername2().equals(username)) {
            throw new FriendshipException("Du bist nicht Teil dieser Freundschaft");
        }

        if (friendship.getStatus() != FriendshipStatus.ACCEPTED) {
            throw new FriendshipException("Dies ist keine aktive Freundschaft");
        }

        FriendRequestDTO dto = new FriendRequestDTO(
                friendship.getId(),
                friendship.getUsername1()
        );

        friendshipPort.delete(friendship);

        eventPort.publishFriendRequestEvent(dto, FriendUpdateType.FRIEND_REMOVED);
        log.info("Friendship {} removed by user {}", friendshipId, username);
    }

    @Override
    public void userConnected(String username) {
        if (username != null && !username.isBlank()) {

            eventPort.publishOnlineStatus(username, true);
            log.info("User {} is now online", username);
        }
    }

    @Override
    public void userDisconnected(String username) {
        if (username != null && !username.isBlank()) {
            eventPort.publishOnlineStatus(username, false);
            log.info("User {} is now offline", username);
        }
    }

    @Override
    public boolean isUserOnline(String username) {
        if (username == null || username.isBlank()) return false;
        return onlineUserPort.isUserOnline(username);
    }

    @Override
    public void inviteToLobby(String lobbyId, String fromUsername, String toUsername) {
        if (toUsername == null || toUsername.isBlank()) {
            throw new FriendshipException("Zielbenutzer fehlt");
        }

        if (fromUsername.equalsIgnoreCase(toUsername)) {
            throw new FriendshipException("Du kannst dich nicht selbst einladen");
        }

        eventPort.publishLobbyInvite(lobbyId, fromUsername, toUsername);

    }

    @Override
    public Friendship getFriendshipById(Long friendshipId) {
        return friendshipPort.findById(friendshipId)
                .orElseThrow(() -> new FriendshipNotFoundException("Freundschaft nicht gefunden"));
    }

}
