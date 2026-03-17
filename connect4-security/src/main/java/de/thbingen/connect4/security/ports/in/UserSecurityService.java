package de.thbingen.connect4.security.ports.in;

import de.thbingen.connect4.common.exception.UsernameTakenException;
import de.thbingen.connect4.security.model.entity.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserSecurityService {
    boolean usernameExists(String username);

    User createUser(User user) throws UsernameTakenException;

    Optional<User> getUserByUsername(String username);

    Optional<User> getUserById(Long id);

    Map<Long, String> getUsernamesByIds(List<Long> userIds);
}
