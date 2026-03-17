package de.thbingen.connect4.chat.service;

import de.thbingen.connect4.chat.model.dto.ChatMessageOutboundDTO;
import de.thbingen.connect4.chat.ports.in.ChatSimpMessagingService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatSimpMessagingServiceImpl implements ChatSimpMessagingService {
    private final static String CHAT_TOPIC_GLOBAL = "/topic/chat/global";
    private final static String CHAT_TOPIC_LOBBY_FORMAT = "/topic/chat/lobby/%s";

    private final SimpMessagingTemplate messaging;

    @Override
    public void sendGlobal(ChatMessageOutboundDTO outboundDTO) {
        messaging.convertAndSend(CHAT_TOPIC_GLOBAL, outboundDTO);
    }

    @Override
    public void sendLobby(ChatMessageOutboundDTO outboundDTO) {
        messaging.convertAndSend(String.format(CHAT_TOPIC_LOBBY_FORMAT, outboundDTO.targetId()), outboundDTO);
    }
}
