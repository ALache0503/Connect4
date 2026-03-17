package de.thbingen.connect4.chat.service;

import de.thbingen.connect4.chat.model.dto.ChatMessage;
import de.thbingen.connect4.chat.model.dto.ChatMessageInboundDTO;
import de.thbingen.connect4.chat.model.enums.ChatType;
import de.thbingen.connect4.chat.ports.in.ChatService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ChatWebSocketServiceImplTest {

    @Mock
    ChatService chatService;

    @InjectMocks
    ChatWebSocketServiceImpl service;

    @Test
    void sendMessage_global_routesToCreateGlobalMessage() {
        var dto = new ChatMessageInboundDTO("hi", ChatType.GLOBAL);

        service.sendMessage("global", dto, "alice");

        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatService).createGlobalMessage(captor.capture());
        verifyNoMoreInteractions(chatService);

        ChatMessage msg = captor.getValue();
        assertEquals("global", msg.targetId());
        assertEquals("hi", msg.text());
        assertEquals("alice", msg.author());
        assertEquals(ChatType.GLOBAL, msg.type());
        assertNotNull(msg.createdAt());
    }

    @Test
    void sendMessage_lobby_routesToCreateLobbyMessage() {
        var dto = new ChatMessageInboundDTO("hi", ChatType.LOBBY);

        service.sendMessage("123", dto, "alice");

        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatService).createLobbyMessage(captor.capture());
        verifyNoMoreInteractions(chatService);

        ChatMessage msg = captor.getValue();
        assertEquals("123", msg.targetId());
        assertEquals("hi", msg.text());
        assertEquals("alice", msg.author());
        assertEquals(ChatType.LOBBY, msg.type());
        assertNotNull(msg.createdAt());
    }

    @Test
    void sendMessage_dm_routesToCreatePrivateMessage() {
        var dto = new ChatMessageInboundDTO("hi", ChatType.DM);

        service.sendMessage("bob", dto, "alice");

        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatService).createPrivateMessage(captor.capture());
        verifyNoMoreInteractions(chatService);

        ChatMessage msg = captor.getValue();
        assertEquals("bob", msg.targetId());
        assertEquals("hi", msg.text());
        assertEquals("alice", msg.author());
        assertEquals(ChatType.DM, msg.type());
        assertNotNull(msg.createdAt());
    }
}
