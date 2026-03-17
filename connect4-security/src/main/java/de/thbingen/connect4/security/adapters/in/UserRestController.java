package de.thbingen.connect4.security.adapters.in;

import de.thbingen.connect4.common.model.dto.UserDTO;
import de.thbingen.connect4.security.exception.UserNotFoundException;
import de.thbingen.connect4.security.model.entity.User;
import de.thbingen.connect4.security.ports.in.UserDTOMapper;
import de.thbingen.connect4.security.ports.in.UserSecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserRestController {
    private final UserSecurityService userSecurityService;
    private final UserDTOMapper userDTOMapper;

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long userId) throws UserNotFoundException {
        UserDTO userDto = userSecurityService.getUserById(userId).map(userDTOMapper::toDto).orElseThrow(UserNotFoundException::new);

        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/auth/username")
    public ResponseEntity<String> getUsername(Principal principal) throws UserNotFoundException {
        if (principal == null) throw new UserNotFoundException();

        String username = userSecurityService.getUserByUsername(principal.getName()).map(userDTOMapper::toDto).orElseThrow(UserNotFoundException::new).username();

        return ResponseEntity.ok(username);
    }

    @GetMapping(value = "/id/{username}")
    public ResponseEntity<?> getUser(@PathVariable String username) {

        Optional<User> user = userSecurityService.getUserByUsername(username);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.of(user.map(User::getId));
    }

    @PostMapping(value = "/batch")
    public ResponseEntity<Map<Long, String>> getUsernamesByIds(
            @RequestBody List<Long> userIds) {

        if (userIds == null || userIds.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Map<Long, String> usernames = userSecurityService.getUsernamesByIds(userIds);

        return ResponseEntity.ok(usernames);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
