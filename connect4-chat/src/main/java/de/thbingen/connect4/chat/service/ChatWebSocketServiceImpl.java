package de.thbingen.connect4.chat.service;

import de.thbingen.connect4.chat.model.dto.ChatMessage;
import de.thbingen.connect4.chat.model.dto.ChatMessageInboundDTO;
import de.thbingen.connect4.chat.ports.in.ChatService;
import de.thbingen.connect4.chat.ports.in.ChatWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class ChatWebSocketServiceImpl implements ChatWebSocketService {

    private final ChatService chatService;

    @Override
    public void sendMessage(String channelId, ChatMessageInboundDTO dto, String username) {
        ChatMessage msg = new ChatMessage(channelId, dto.text(), username, new Date(), dto.chatType());

        switch (dto.chatType()) {
            case DM -> chatService.createPrivateMessage(msg);
            case LOBBY -> chatService.createLobbyMessage(msg);
            case GLOBAL -> chatService.createGlobalMessage(msg);
        }

    }
}
