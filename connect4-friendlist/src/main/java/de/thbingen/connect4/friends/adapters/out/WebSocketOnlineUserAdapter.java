package de.thbingen.connect4.friends.adapters.out;

import de.thbingen.connect4.friends.ports.out.OnlineUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketOnlineUserAdapter implements OnlineUserPort {

    private final SimpUserRegistry simpUserRegistry;

    @Override
    public boolean isUserOnline(String username) {
        return simpUserRegistry.getUser(username) != null;
    }
}
