package de.thbingen.connect4.common.filter;

import de.thbingen.connect4.common.model.dto.UserDTO;
import de.thbingen.connect4.common.ports.in.JwtUtilService;
import de.thbingen.connect4.common.ports.out.UserService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JWTHandshakeInterceptor implements HandshakeInterceptor {
    private final JwtUtilService jwtUtilService;
    private final UserService userService;

    @Override
    public boolean beforeHandshake(@Nullable ServerHttpRequest request,
                                   @Nullable ServerHttpResponse response,
                                   @Nullable WebSocketHandler wsHandler,
                                   @Nullable Map<String, Object> attributes) {

        if (request == null || response == null || wsHandler == null || attributes == null) {
            return false;
        }

        HttpHeaders headers = request.getHeaders();

        // Alle Cookies als String aus Header "Cookie"
        String cookieHeader = headers.getFirst(HttpHeaders.COOKIE);
        if (cookieHeader == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        // ACCESS_TOKEN extrahieren
        String token = Arrays.stream(cookieHeader.split(";"))
                .map(String::trim)
                .filter(s -> s.startsWith("ACCESS_TOKEN="))
                .map(s -> s.substring("ACCESS_TOKEN=".length()))
                .findFirst()
                .orElse(null);

        if (token == null || !jwtUtilService.validateToken(token)) {

            return false;
        }

        String userId = jwtUtilService.extractUserId(token);

        Optional<UserDTO> optionalUser = userService.getUserById(Long.valueOf(userId));
        if (optionalUser.isEmpty()) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        UserDTO user = optionalUser.get();
        attributes.put("user", user.username());

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, @Nullable Exception exception) {
        // ignore
    }
}
