package de.thbingen.connect4.common.filter;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JWTHandshakeHandler extends DefaultHandshakeHandler {

    private final UserDetailsService userDetailsService;

    @Override
    protected @Nullable Principal determineUser(@Nullable ServerHttpRequest request, @Nullable WebSocketHandler wsHandler, @Nullable Map<String, Object> attributes) {
        if (request != null && wsHandler != null && attributes != null) {
            String username = (String) attributes.get("user");

            if (username != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            }
        }

        return null;
    }
}
