package de.thbingen.connect4.common.ports.out;

import de.thbingen.connect4.common.model.dto.UserDTO;

import java.util.Optional;

public interface UserService {
    Optional<UserDTO> getUserById(Long id);
}
