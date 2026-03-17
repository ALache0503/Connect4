package de.thbingen.connect4.chat.ports.in;

import de.thbingen.connect4.chat.model.dto.ChatMessageOutboundDTO;

public interface ChatSimpMessagingService {
    void sendGlobal(ChatMessageOutboundDTO outboundDTO);

    void sendLobby(ChatMessageOutboundDTO outboundDTO);
}
