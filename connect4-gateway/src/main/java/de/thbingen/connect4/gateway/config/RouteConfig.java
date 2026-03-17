package de.thbingen.connect4.gateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RouteConfig {

    @Value("${services.security.url:http://localhost:9001}")
    private String securityServiceUrl;

    @Value("${services.statistics.url:http://localhost:9005}")
    private String statisticsServiceUrl;

    @Value("${services.matchmaking.url:http://localhost:9002}")
    private String matchmakingServiceUrl;

    @Value("${services.matchmaking.websocket.url:ws://localhost:9002/ws}")
    private String matchmakingServiceWebsocketUrl;

    @Value("${services.gaming.url:http://localhost:9003}")
    private String gamingServiceUrl;

    @Value("${services.gaming.websocket.url:ws://localhost:9003/ws}")
    private String gamingServiceWebsocketUrl;

    @Value("${services.chat.url:http://localhost:9006}")
    private String chatServiceUrl;

    @Value("${services.chat.websocket.url:ws://localhost:9006/ws}")
    private String chatServiceWebsocketUrl;

    @Value("${services.friendlist.url:http://localhost:9004}")
    private String friendlistServiceUrl;

    @Value("${services.friendlist.websocket.url:ws://localhost:9004/ws}")
    private String friendlistServiceWebsocketUrl;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        System.out.println("=== RouteConfig: Loading routes including friendlist-service-websocket ===");
        return builder.routes()
                .route("security-service-auth", p -> p
                        .path("/api/v1/auth/**")
                        .uri(securityServiceUrl)
                )
                .route("security-service-users", p -> p
                        .path("/api/v1/users/**")
                        .uri(securityServiceUrl)
                )
                .route("matchmaking-service", p -> p
                        .path("/api/v1/mm/**")
                        .uri(matchmakingServiceUrl)
                )
                .route("gaming-service-lobby", p -> p
                        .path("/api/v1/lobby/**")
                        .uri(gamingServiceUrl)
                )
                .route("gaming-service-game", p -> p
                        .path("/api/v1/game/**")
                        .uri(gamingServiceUrl)
                )
                .route("chat-service", p -> p
                        .path("/api/v1/chat/**")
                        .uri(chatServiceUrl)
                )
                .route("gaming-service-websocket", p -> p
                        .path("/gaming/ws/**")
                        .filters(f -> f.rewritePath("/gaming/(?<remaining>.*)", "/${remaining}"))
                        .uri(gamingServiceWebsocketUrl)
                )
                .route("matchmaking-service-websocket", p -> p
                        .path("/mm/ws/**")
                        .filters(f -> f.rewritePath("/mm/(?<remaining>.*)", "/${remaining}"))
                        .uri(matchmakingServiceWebsocketUrl)
                )
                .route("statistics-service", p -> p
                        .path("/api/v1/statistics/**")
                        .uri(statisticsServiceUrl)
                )
                .route("chat-service-websocket", p -> p
                        .path("/chat/ws/**")
                        .filters(f -> f.rewritePath("/chat/(?<remaining>.*)", "/${remaining}"))
                        .uri(chatServiceWebsocketUrl)
                )
                .route("friendlist-service", p -> p
                        .path("/api/v1/friends/**")
                        .uri(friendlistServiceUrl)
                )
                .route("friendlist-service-websocket", p -> p
                        .path("/friends/ws/**")
                        .filters(f -> f.rewritePath("/friends/(?<remaining>.*)", "/${remaining}")
                        )
                        .uri(friendlistServiceWebsocketUrl)
                )
                .build();
    }
}
