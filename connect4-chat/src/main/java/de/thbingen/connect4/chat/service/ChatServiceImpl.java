package de.thbingen.connect4.chat.service;

import de.thbingen.connect4.chat.model.dto.ChatMessage;
import de.thbingen.connect4.chat.model.dto.ChatMessageOutboundDTO;
import de.thbingen.connect4.chat.ports.in.ChatMessageMapper;
import de.thbingen.connect4.chat.ports.in.ChatService;
import de.thbingen.connect4.chat.ports.in.ChatSimpMessagingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {


    private final ChatSimpMessagingService chatSimpMessagingService;
    private final ChatMessageMapper chatMessageMapper;

    public void createGlobalMessage(ChatMessage chatMessage) {
        ChatMessageOutboundDTO outboundDTO = chatMessageMapper.toOutboundDto(chatMessage);

        chatSimpMessagingService.sendGlobal(outboundDTO);
    }

    @Override
    public void createPrivateMessage(ChatMessage chatMessage) {
        // TODO wait for friendlist
    }

    @Override
    public void createLobbyMessage(ChatMessage chatMessage) {
        ChatMessageOutboundDTO outboundDTO = chatMessageMapper.toOutboundDto(chatMessage);

        chatSimpMessagingService.sendLobby(outboundDTO);
    }
}
