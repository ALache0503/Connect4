package de.thbingen.connect4.chat.model.dto;

import de.thbingen.connect4.chat.model.enums.ChatType;

import java.util.Date;

public record ChatMessageOutboundDTO(String targetId, String text, String author, Date createdAt, ChatType type) {
}
