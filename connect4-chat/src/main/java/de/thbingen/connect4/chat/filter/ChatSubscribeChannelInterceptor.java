package de.thbingen.connect4.chat.filter;

import de.thbingen.connect4.chat.ports.out.LobbyService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatSubscribeChannelInterceptor implements ChannelInterceptor {

    private final LobbyService lobbyService;

    @Override
    public @Nullable Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(message);

        if (StompCommand.SUBSCRIBE.equals(acc.getCommand())) {
            String dest = acc.getDestination();
            String lobbyId = extractLobbyId(dest);

            if (lobbyId != null) {
                var user = acc.getUser();
                String username = (user != null ? user.getName() : null);

                if (username == null || !lobbyService.isMember(lobbyId, username)) {
                    throw new AccessDeniedException("Not a lobby member");
                }
            }
        }

        return message;
    }

    private String extractLobbyId(String dest) {
        if (dest == null) return null;
        String prefix = "/topic/chat/lobby/";

        if (!dest.startsWith(prefix)) return null;
        try {
            return dest.substring(prefix.length());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
