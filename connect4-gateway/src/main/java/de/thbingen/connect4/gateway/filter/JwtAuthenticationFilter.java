package de.thbingen.connect4.gateway.filter;

import de.thbingen.connect4.gateway.ports.in.JwtUtilService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter {

    private final JwtUtilService jwtUtilService;

    @Value("${security.public-paths}")
    List<String> publicPaths;

    @NotNull
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, @NotNull GatewayFilterChain chain) {
        HttpCookie accessTokenCookie = exchange.getRequest()
                .getCookies()
                .getFirst("ACCESS_TOKEN");

        String path = exchange.getRequest().getPath().toString();
        log.info("Request path: {}", path);

        if (isPublicPath(path)) {
            log.info("Skipping JWT check for path: {}", path);
            return chain.filter(exchange);
        }

        //Does cookie exist?
        if (accessTokenCookie == null) {
            return onError(exchange, "Kein ACCESS_TOKEN Cookie vorhanden", HttpStatus.UNAUTHORIZED);
        }

        //extract token from header
        String token = accessTokenCookie.getValue();
        if (!jwtUtilService.validateToken(token)) {
            return onError(exchange, "JWT Token ist ungültig", HttpStatus.UNAUTHORIZED);
        }

        ServerHttpRequest request = exchange.getRequest().mutate()
                .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s ", token)).build();

        // token is valid, forward request
        return chain.filter(exchange.mutate().request(request).build());
    }

    private boolean isPublicPath(String path) {
        for (String cString : publicPaths) {
            if (path.startsWith(cString)) {
                return true;
            }
        }

        return false;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");

        String errorResponse = String.format("{\"error\": \"%s\", \"status\": %d}",
                message, status.value());
        DataBuffer buffer = response.bufferFactory()
                .wrap(errorResponse.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }
}
