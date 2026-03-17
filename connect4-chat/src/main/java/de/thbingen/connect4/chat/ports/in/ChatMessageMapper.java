package de.thbingen.connect4.chat.ports.in;

import de.thbingen.connect4.chat.model.dto.ChatMessage;
import de.thbingen.connect4.chat.model.dto.ChatMessageOutboundDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {
    ChatMessageOutboundDTO toOutboundDto(ChatMessage msg);
}
