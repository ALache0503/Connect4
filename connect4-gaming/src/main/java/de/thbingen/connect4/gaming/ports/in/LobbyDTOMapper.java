package de.thbingen.connect4.gaming.ports.in;

import de.thbingen.connect4.gaming.model.dto.LobbyDTO;
import de.thbingen.connect4.gaming.model.entity.Lobby;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LobbyDTOMapper {

    LobbyDTO toDto(Lobby lobby);
}
