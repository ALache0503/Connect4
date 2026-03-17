package de.thbingen.connect4.security.ports.out;

import de.thbingen.connect4.security.model.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSecurityRepository extends CrudRepository<User, Long> {
    boolean existsByUsernameIgnoreCase(String username);

    Optional<User> findByUsernameIgnoreCase(String username);

    List<User> findByIdIn(List<Long> ids);
}
