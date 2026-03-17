package de.thbingen.connect4.chat.filter;

import de.thbingen.connect4.chat.ports.out.LobbyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatSubscribeChannelInterceptorTest {

    @Mock
    LobbyService lobbyService;

    @Mock
    MessageChannel channel;

    private static Message<byte[]> stompSubscribe(String destination, Principal user) {
        StompHeaderAccessor acc = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        acc.setDestination(destination);
        if (user != null) acc.setUser(user);

        return MessageBuilder.createMessage(new byte[0], acc.getMessageHeaders());
    }

    @Test
    void preSend_allowsSubscribe_whenNotLobbyDestination() {
        var interceptor = new ChatSubscribeChannelInterceptor(lobbyService);

        Message<byte[]> msg = stompSubscribe("/topic/chat/global", () -> "alice");

        Message<?> result = interceptor.preSend(msg, channel);

        assertSame(msg, result);
        verifyNoInteractions(lobbyService);
    }

    @Test
    void preSend_allowsSubscribe_whenUserIsLobbyMember() {
        var interceptor = new ChatSubscribeChannelInterceptor(lobbyService);

        when(lobbyService.isMember("123", "alice")).thenReturn(true);

        Message<byte[]> msg = stompSubscribe("/topic/chat/lobby/123", () -> "alice");

        Message<?> result = interceptor.preSend(msg, channel);

        assertSame(msg, result);
        verify(lobbyService).isMember("123", "alice");
        verifyNoMoreInteractions(lobbyService);
    }

    @Test
    void preSend_deniesSubscribe_whenUserIsNotLobbyMember() {
        var interceptor = new ChatSubscribeChannelInterceptor(lobbyService);

        when(lobbyService.isMember("123", "alice")).thenReturn(false);

        Message<byte[]> msg = stompSubscribe("/topic/chat/lobby/123", () -> "alice");

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(msg, channel));
        verify(lobbyService).isMember("123", "alice");
        verifyNoMoreInteractions(lobbyService);
    }

    @Test
    void preSend_deniesSubscribe_whenUserIsNull() {
        var interceptor = new ChatSubscribeChannelInterceptor(lobbyService);

        Message<byte[]> msg = stompSubscribe("/topic/chat/lobby/123", null);

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(msg, channel));
        verifyNoInteractions(lobbyService);
    }
}
