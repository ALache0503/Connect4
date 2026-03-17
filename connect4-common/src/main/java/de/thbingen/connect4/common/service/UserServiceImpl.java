package de.thbingen.connect4.common.service;

import de.thbingen.connect4.common.model.dto.UserDTO;
import de.thbingen.connect4.common.ports.out.UserRepository;
import de.thbingen.connect4.common.ports.out.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.getUserById(id);
    }
}
