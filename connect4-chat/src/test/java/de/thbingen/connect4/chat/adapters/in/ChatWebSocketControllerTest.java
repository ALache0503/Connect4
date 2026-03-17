package de.thbingen.connect4.chat.adapters.in;

import de.thbingen.connect4.chat.exception.ChatNotFoundException;
import de.thbingen.connect4.chat.model.dto.ChatMessageInboundDTO;
import de.thbingen.connect4.chat.model.enums.ChatType;
import de.thbingen.connect4.chat.ports.in.ChatWebSocketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatWebSocketControllerTest {

    @Mock
    ChatWebSocketService chatWebSocketService;

    @InjectMocks
    ChatWebSocketController controller;

    @Test
    void sendMessage_delegatesToService() throws ChatNotFoundException {
        // arrange
        String targetId = "global";
        ChatMessageInboundDTO dto = new ChatMessageInboundDTO("hi", ChatType.GLOBAL);
        Principal principal = () -> "alice";

        // act
        controller.sendMessage(targetId, dto, principal);

        // assert
        verify(chatWebSocketService).sendMessage(targetId, dto, "alice");
        verifyNoMoreInteractions(chatWebSocketService);
    }

    @Test
    void sendMessage_ignoresChatNotFoundException() throws ChatNotFoundException {
        // arrange
        String targetId = "lobby-1";
        ChatMessageInboundDTO dto = new ChatMessageInboundDTO("hi", ChatType.LOBBY);
        Principal principal = () -> "alice";

        doThrow(new ChatNotFoundException())
                .when(chatWebSocketService)
                .sendMessage(targetId, dto, "alice");

        // act + assert
        controller.sendMessage(targetId, dto, principal);

        verify(chatWebSocketService).sendMessage(targetId, dto, "alice");
    }
}
