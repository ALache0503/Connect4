package de.thbingen.connect4.chat.model.dto;

import de.thbingen.connect4.chat.model.enums.ChatType;

public record ChatMessageInboundDTO(String text, ChatType chatType) {

}
