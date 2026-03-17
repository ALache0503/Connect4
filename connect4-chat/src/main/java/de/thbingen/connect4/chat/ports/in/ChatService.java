package de.thbingen.connect4.chat.ports.in;

import de.thbingen.connect4.chat.model.dto.ChatMessage;

public interface ChatService {
    void createGlobalMessage(ChatMessage chatMessage);

    void createPrivateMessage(ChatMessage chatMessage);

    void createLobbyMessage(ChatMessage chatMessage);
}
