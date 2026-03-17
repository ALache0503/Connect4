package de.thbingen.connect4.chat.ports.out;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "gaming-service",
        url = "${services.gaming-service.url}",
        path = "/api/v1/lobby"
)
public interface LobbyRepository {

    @GetMapping("/checkMember/{lobbyId}/{username}")
    Boolean isMember(@PathVariable String lobbyId, @PathVariable String username);
}
