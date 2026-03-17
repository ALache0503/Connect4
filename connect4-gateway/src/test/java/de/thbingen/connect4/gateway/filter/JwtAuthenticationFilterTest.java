package de.thbingen.connect4.gateway.filter;

import de.thbingen.connect4.gateway.ports.in.JwtUtilService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtilService jwtUtilService;

    @Mock
    private GatewayFilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtAuthenticationFilter, "publicPaths", List.of("/api/public", "/login"));

        lenient().when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
    }

    @Test
    void filter_ShouldAllowPublicPathWithoutToken() {
        // Arrange
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/public/info").build()
        );

        // Act
        jwtAuthenticationFilter.filter(exchange, filterChain).block();

        // Assert
        verify(filterChain).filter(exchange);
        verifyNoInteractions(jwtUtilService);
    }

    @Test
    void filter_ShouldReturnUnauthorized_WhenTokenMissing() {
        // Arrange
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/private/data").build()
        );

        // Act
        jwtAuthenticationFilter.filter(exchange, filterChain).block();

        // Assert
        verify(filterChain, never()).filter(any(ServerWebExchange.class));
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_ShouldReturnUnauthorized_WhenTokenInvalid() {
        // Arrange
        HttpCookie cookie = new HttpCookie("ACCESS_TOKEN", "invalid-token");
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/private/data")
                        .cookie(cookie)
                        .build()
        );

        when(jwtUtilService.validateToken("invalid-token")).thenReturn(false);

        // Act
        jwtAuthenticationFilter.filter(exchange, filterChain).block();

        // Assert
        verify(filterChain, never()).filter(any(ServerWebExchange.class));
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_ShouldForwardRequestWithAuthHeader_WhenTokenValid() {
        // Arrange
        String validToken = "valid-jwt-token";
        HttpCookie cookie = new HttpCookie("ACCESS_TOKEN", validToken);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/private/data")
                        .cookie(cookie)
                        .build()
        );

        when(jwtUtilService.validateToken(validToken)).thenReturn(true);

        // Act
        jwtAuthenticationFilter.filter(exchange, filterChain).block();

        // Assert
        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(filterChain).filter(captor.capture());

        ServerWebExchange forwardedExchange = captor.getValue();
        String authHeader = forwardedExchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        assertEquals("Bearer valid-jwt-token ", authHeader);
        assertNull(exchange.getResponse().getStatusCode());
    }
}
