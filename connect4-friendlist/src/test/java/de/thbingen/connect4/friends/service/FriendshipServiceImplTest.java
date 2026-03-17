package de.thbingen.connect4.friends.service;

import de.thbingen.connect4.friends.exception.FriendshipException;
import de.thbingen.connect4.friends.exception.FriendshipNotFoundException;
import de.thbingen.connect4.friends.model.dto.FriendDTO;
import de.thbingen.connect4.friends.model.entity.Friendship;
import de.thbingen.connect4.friends.model.enums.FriendUpdateType;
import de.thbingen.connect4.friends.model.enums.FriendshipStatus;
import de.thbingen.connect4.friends.ports.out.FriendshipEventPort;
import de.thbingen.connect4.friends.ports.out.FriendshipPersistencePort;
import de.thbingen.connect4.friends.ports.out.OnlineUserPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendshipServiceImplTest {

    @Mock
    FriendshipPersistencePort persistencePort;

    @Mock
    FriendshipEventPort eventPort;

    @Mock
    OnlineUserPort onlineUserPort;

    @InjectMocks
    FriendshipServiceImpl service;

    @Test
    void sendFriendRequest_ShouldCreatePendingFriendship() {
        Friendship saved = new Friendship(1L, "a", "b", FriendshipStatus.PENDING, null, null);
        when(persistencePort.findFriendshipBetweenUsers("a", "b")).thenReturn(Optional.empty());
        when(persistencePort.save(any())).thenReturn(saved);

        service.sendFriendRequest("a", "b");

        verify(eventPort).publishFriendRequestEvent(any(), eq(FriendUpdateType.REQUEST_RECEIVED));
    }

    @Test
    void sendFriendRequest_ShouldThrow_WhenSelfRequest() {
        assertThrows(FriendshipException.class, () ->
                service.sendFriendRequest("a", "a")
        );
    }

    @Test
    void acceptFriendRequest_ShouldAccept() {
        Friendship f = new Friendship(1L, "a", "b", FriendshipStatus.PENDING, null, null);
        when(persistencePort.findById(1L)).thenReturn(Optional.of(f));
        when(persistencePort.save(any())).thenReturn(f);

        service.acceptFriendRequest("b", 1L);

        assertEquals(FriendshipStatus.ACCEPTED, f.getStatus());
        verify(eventPort).publishFriendRequestEvent(any(), eq(FriendUpdateType.REQUEST_ACCEPTED));
    }

    @Test
    void removeFriend_ShouldDeleteAndPublishEvent() {
        Friendship f = new Friendship(1L, "a", "b", FriendshipStatus.ACCEPTED, null, null);
        when(persistencePort.findById(1L)).thenReturn(Optional.of(f));

        service.removeFriend("a", 1L);

        verify(persistencePort).delete(f);
        verify(eventPort).publishFriendRequestEvent(any(), eq(FriendUpdateType.FRIEND_REMOVED));
    }

    @Test
    void getFriends_ShouldReturnOnlineStatus() {
        Friendship f = new Friendship(1L, "a", "b", FriendshipStatus.ACCEPTED, null, null);
        when(persistencePort.findAcceptedFriendshipsByUsername("a")).thenReturn(List.of(f));
        when(onlineUserPort.isUserOnline("b")).thenReturn(true);

        List<FriendDTO> friends = service.getFriends("a");

        assertEquals(1, friends.size());
        assertTrue(friends.get(0).online());
    }

    @Test
    void getFriendshipById_ShouldThrow_WhenNotFound() {
        when(persistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThrows(FriendshipNotFoundException.class, () ->
                service.getFriendshipById(99L)
        );
    }

    @Test
    void sendFriendRequest_ShouldThrow_WhenFriendshipAlreadyExists() {
        Friendship existing = new Friendship(
                1L, "a", "b", FriendshipStatus.PENDING, null, null
        );

        when(persistencePort.findFriendshipBetweenUsers("a", "b"))
                .thenReturn(Optional.of(existing));

        assertThrows(FriendshipException.class, () ->
                service.sendFriendRequest("a", "b")
        );

        verify(persistencePort, never()).save(any());
        verify(eventPort, never()).publishFriendRequestEvent(any(), any());
    }

    @Test
    void acceptFriendRequest_ShouldThrow_WhenUserIsNotRecipient() {
        Friendship f = new Friendship(
                1L, "sender", "recipient", FriendshipStatus.PENDING, null, null
        );

        when(persistencePort.findById(1L)).thenReturn(Optional.of(f));

        assertThrows(FriendshipException.class, () ->
                service.acceptFriendRequest("otherUser", 1L)
        );

        verify(persistencePort, never()).save(any());
        verify(eventPort, never()).publishFriendRequestEvent(any(), any());
    }

    @Test
    void acceptFriendRequest_ShouldThrow_WhenStatusIsNotPending() {
        Friendship f = new Friendship(
                1L, "a", "b", FriendshipStatus.ACCEPTED, null, null
        );

        when(persistencePort.findById(1L)).thenReturn(Optional.of(f));

        assertThrows(FriendshipException.class, () ->
                service.acceptFriendRequest("b", 1L)
        );

        verify(eventPort, never()).publishFriendRequestEvent(any(), any());
    }

    @Test
    void getFriends_ShouldReturnCorrectOtherUser() {
        Friendship f = new Friendship(
                1L, "a", "b", FriendshipStatus.ACCEPTED, null, null
        );

        when(persistencePort.findAcceptedFriendshipsByUsername("b"))
                .thenReturn(List.of(f));
        when(onlineUserPort.isUserOnline("a")).thenReturn(false);

        List<FriendDTO> result = service.getFriends("b");

        assertEquals(1, result.size());
        assertEquals("a", result.get(0).username());
    }


}
