package de.thbingen.connect4.security.service;

import de.thbingen.connect4.common.exception.UsernameTakenException;
import de.thbingen.connect4.common.ports.out.UserService;
import de.thbingen.connect4.security.model.entity.User;
import de.thbingen.connect4.security.ports.in.UserSecurityService;
import de.thbingen.connect4.security.ports.out.UserSecurityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserSecurityServiceImpl implements UserSecurityService {

    private final UserSecurityRepository userSecurityRepository;
    private final UserService userService;

    @Override
    public boolean usernameExists(String username) {
        return userSecurityRepository.existsByUsernameIgnoreCase(username);
    }

    @Override
    public User createUser(User user) throws UsernameTakenException {
        return userSecurityRepository.save(user);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return userSecurityRepository.findByUsernameIgnoreCase(username);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userSecurityRepository.findById(id);
    }

    @Override
    public Map<Long, String> getUsernamesByIds(List<Long> userIds) {
        //return userSecurityRepository.getUsernamesByIds(userIds);

        Iterable<User> users = userSecurityRepository.findAllById(userIds);
        List<User> userList = new ArrayList<>();
        users.forEach(userList::add);
        return userList.stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
    }
}
