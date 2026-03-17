package de.thbingen.connect4.security.adapters.in;

import de.thbingen.connect4.common.exception.UsernameTakenException;
import de.thbingen.connect4.common.filter.JWTFilter;
import de.thbingen.connect4.common.filter.JWTHandshakeInterceptor;
import de.thbingen.connect4.common.model.dto.AuthRequest;
import de.thbingen.connect4.common.model.dto.AuthResponse;
import de.thbingen.connect4.common.ports.out.UserRepository;
import de.thbingen.connect4.security.config.SecurityConfig;
import de.thbingen.connect4.security.exception.LoginFailedException;
import de.thbingen.connect4.security.ports.in.SecurityService;
import de.thbingen.connect4.security.ports.in.UserSecurityService;
import de.thbingen.connect4.security.ports.out.UserSecurityRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthenticationRestController.class, properties = {
        "jwt.expiration=3600",
        "services.security-service.url=http://localhost:9001" // DUMMY
})
@Import(SecurityConfig.class)
class AuthenticationRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SecurityService securityService;

    @MockitoBean
    private UserSecurityService userSecurityService; // Damit UserRestController zufrieden ist

    @MockitoBean
    private UserSecurityRepository userSecurityRepository;

    @MockitoBean
    private JWTFilter jwtFilter;

    @MockitoBean
    private JWTHandshakeInterceptor jwtHandshakeInterceptor;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() throws ServletException, IOException {
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);

            chain.doFilter(request, response);
            return null;
        }).when(jwtFilter).doFilter(any(), any(), any());
    }

    @Test
    void register_ShouldReturnOkAndSetCookies_WhenSuccessful() throws Exception {
        AuthRequest request = new AuthRequest("user1", "pass123");
        AuthResponse response = new AuthResponse("access-token", "refresh-token");

        when(securityService.register(any(AuthRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(cookie().value("ACCESS_TOKEN", "access-token"))
                .andExpect(cookie().httpOnly("ACCESS_TOKEN", true))
                .andExpect(cookie().path("ACCESS_TOKEN", "/"))

                .andExpect(cookie().value("REFRESH_TOKEN", "refresh-token"))
                .andExpect(cookie().httpOnly("REFRESH_TOKEN", true))
                .andExpect(cookie().path("REFRESH_TOKEN", "/api/v1/auth/refresh"));
    }


    @Test
    void register_ShouldReturnConflict_WhenUsernameTaken() throws Exception {
        AuthRequest request = new AuthRequest("existingUser", "pass");

        when(securityService.register(any(AuthRequest.class)))
                .thenThrow(new UsernameTakenException());

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_ShouldReturnOkAndSetCookies_WhenSuccessful() throws Exception {
        AuthRequest request = new AuthRequest("user1", "pass123");
        AuthResponse response = new AuthResponse("new-access", "new-refresh");

        when(securityService.login(any(AuthRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(cookie().value("ACCESS_TOKEN", "new-access"))
                .andExpect(cookie().httpOnly("ACCESS_TOKEN", true))
                .andExpect(cookie().path("ACCESS_TOKEN", "/"))

                .andExpect(cookie().value("REFRESH_TOKEN", "new-refresh"))
                .andExpect(cookie().httpOnly("REFRESH_TOKEN", true))
                .andExpect(cookie().path("REFRESH_TOKEN", "/api/v1/auth/refresh"));
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenCredentialsInvalid() throws Exception {
        AuthRequest request = new AuthRequest("user1", "wrongpass");

        when(securityService.login(any(AuthRequest.class)))
                .thenThrow(new LoginFailedException());

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_ShouldReturnOkAndNewCookies_WhenTokenValid() throws Exception {
        String refreshToken = "valid-refresh-token";
        AuthResponse response = new AuthResponse("fresh-access", "fresh-refresh");

        when(securityService.refresh(refreshToken)).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .with(csrf())
                        .cookie(new Cookie("REFRESH_TOKEN", refreshToken))) // Sende Cookie
                .andExpect(status().isOk())
                .andExpect(cookie().value("ACCESS_TOKEN", "fresh-access"))
                .andExpect(cookie().httpOnly("ACCESS_TOKEN", true))
                .andExpect(cookie().path("ACCESS_TOKEN", "/"))

                .andExpect(cookie().value("REFRESH_TOKEN", "fresh-refresh"))
                .andExpect(cookie().httpOnly("REFRESH_TOKEN", true))
                .andExpect(cookie().path("REFRESH_TOKEN", "/api/v1/auth/refresh"));
    }

    @Test
    void refresh_ShouldReturnUnauthorized_WhenCookieMissing() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_ShouldReturnUnauthorized_WhenTokenInvalid() throws Exception {
        String invalidToken = "expired-token";

        when(securityService.refresh(invalidToken)).thenThrow(new JwtException("Expired"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .with(csrf())
                        .cookie(new Cookie("REFRESH_TOKEN", invalidToken)))
                .andExpect(status().isUnauthorized());
    }
}
