package de.thbingen.connect4.chat.ports.in;

import de.thbingen.connect4.chat.exception.ChatNotFoundException;
import de.thbingen.connect4.chat.model.dto.ChatMessageInboundDTO;

public interface ChatWebSocketService {

    void sendMessage(String channelId, ChatMessageInboundDTO msg, String username) throws ChatNotFoundException;
}
