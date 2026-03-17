package de.thbingen.connect4.matchmaking.ports.out;

import de.thbingen.connect4.common.model.dto.CreateLobbyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "gaming-service",
        url = "${services.gaming-service.url}"
)
public interface LobbyRepository {
    @PostMapping("/api/v1/lobby/create")
    CreateLobbyResponse createLobby();
}
