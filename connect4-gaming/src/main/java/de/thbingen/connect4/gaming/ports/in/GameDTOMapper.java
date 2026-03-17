package de.thbingen.connect4.gaming.ports.in;

import de.thbingen.connect4.gaming.model.dto.GameDTO;
import de.thbingen.connect4.gaming.model.entity.Game;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface GameDTOMapper {
    @Mappings(value = {
            @Mapping(target = "lobbyId", source = "lobby.id"),
            @Mapping(target = "currentTurn", source = "currentTurn.username"),
            @Mapping(target = "player1", source = "player1.username"),
            @Mapping(target = "player2", source = "player2.username")
    }
    )
    GameDTO toDto(Game game);
}
