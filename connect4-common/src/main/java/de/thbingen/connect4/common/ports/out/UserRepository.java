package de.thbingen.connect4.common.ports.out;

import de.thbingen.connect4.common.model.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@FeignClient(
        name = "security-service",
        url = "${services.security-service.url}",
        path = "/api/v1/users"
)
public interface UserRepository {
    @GetMapping("/{userId}")
    Optional<UserDTO> getUserById(@PathVariable Long userId);

    @GetMapping(value = "/id/{username}")
    Long getUser(@PathVariable String username);

    @PostMapping(value = "/batch")
    Map<Long, String> getUsernamesByIds(List<Long> userIds);
}
