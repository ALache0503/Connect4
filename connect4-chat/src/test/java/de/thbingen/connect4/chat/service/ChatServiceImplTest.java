package de.thbingen.connect4.chat.service;

import de.thbingen.connect4.chat.model.dto.ChatMessage;
import de.thbingen.connect4.chat.model.dto.ChatMessageOutboundDTO;
import de.thbingen.connect4.chat.model.enums.ChatType;
import de.thbingen.connect4.chat.ports.in.ChatMessageMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Date;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    SimpMessagingTemplate messaging;

    @Mock
    ChatMessageMapper chatMessageMapper;

    @InjectMocks
    ChatServiceImpl service;

    @Test
    void createGlobalMessage_sendsToGlobalTopic() {
        ChatMessage msg = mock(ChatMessage.class);
        ChatMessageOutboundDTO dto = mock(ChatMessageOutboundDTO.class);
        when(chatMessageMapper.toOutboundDto(msg)).thenReturn(dto);

        service.createGlobalMessage(msg);

        verify(messaging).convertAndSend("/topic/chat/global", dto);
        verifyNoMoreInteractions(messaging);
    }

    @Test
    void createLobbyMessage_sendsToLobbyTopicUsingTargetId() {
        ChatMessage msg = new ChatMessage("123", "hello", "testuser", new Date(), ChatType.LOBBY);

        ChatMessageOutboundDTO dto = new ChatMessageOutboundDTO("123", "hello", "testuser", new Date(), ChatType.LOBBY);
        when(chatMessageMapper.toOutboundDto(msg)).thenReturn(dto);

        service.createLobbyMessage(msg);

        verify(messaging).convertAndSend("/topic/chat/lobby/123", dto);
        verifyNoMoreInteractions(messaging);
    }
}
