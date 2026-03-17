package de.thbingen.connect4.security.ports.in;

import de.thbingen.connect4.common.model.dto.UserDTO;
import de.thbingen.connect4.security.model.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserDTOMapper {
    UserDTO toDto(User user);
}
