package de.thbingen.connect4.chat.adapters.in;

import de.thbingen.connect4.chat.exception.ChatNotFoundException;
import de.thbingen.connect4.chat.model.dto.ChatMessageInboundDTO;
import de.thbingen.connect4.chat.ports.in.ChatWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatWebSocketService chatWebSocketService;

    @MessageMapping("/chat.sendMessage/{targetId}")
    public void sendMessage(@DestinationVariable String targetId, ChatMessageInboundDTO msg, Principal principal) {
        try {
            chatWebSocketService.sendMessage(targetId, msg, principal.getName());
        } catch (ChatNotFoundException ignore) {
            // ignore yet
        }
    }
}
