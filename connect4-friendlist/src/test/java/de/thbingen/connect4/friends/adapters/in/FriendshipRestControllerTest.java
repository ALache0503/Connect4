package de.thbingen.connect4.friends.adapters.in;

import de.thbingen.connect4.common.filter.JWTFilter;
import de.thbingen.connect4.friends.adapters.out.SpringDataFriendshipRepository;
import de.thbingen.connect4.friends.model.entity.Friendship;
import de.thbingen.connect4.friends.model.enums.FriendshipStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.sql.Timestamp;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FriendshipRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpringDataFriendshipRepository friendshipRepository;

    @MockitoBean
    private JWTFilter jwtFilter;

    @MockitoBean
    private UserDetailsService userDetailsService;


    @DynamicPropertySource
    static void h2Properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        registry.add("spring.datasource.driverClassName", () -> "org.h2.Driver");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.H2Dialect");
    }

    @BeforeAll
    static void setupTestData(@Autowired SpringDataFriendshipRepository friendshipRepository) {
        friendshipRepository.deleteAll();

        Friendship pending = new Friendship();
        pending.setUsername1("bob");
        pending.setUsername2("alice");
        pending.setStatus(FriendshipStatus.PENDING);
        pending.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        pending.setUpdatedAt(pending.getCreatedAt());
        friendshipRepository.save(pending);

        Friendship accepted = new Friendship();
        accepted.setUsername1("alice");
        accepted.setUsername2("charlie");
        accepted.setStatus(FriendshipStatus.ACCEPTED);
        accepted.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        accepted.setUpdatedAt(accepted.getCreatedAt());
        friendshipRepository.save(accepted);

    }

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
    void testGetFriends() throws Exception {
        mockMvc.perform(get("/api/v1/friends")
                        .with(csrf())
                        .with(user("alice"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("charlie"));
    }

    @Test
    void testGetRequests() throws Exception {
        mockMvc.perform(get("/api/v1/friends/requests")
                        .with(csrf())
                        .with(user("alice"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incoming[0].username").value("bob"));
    }

    @Test
    void testSendFriendRequest() throws Exception {
        mockMvc.perform(post("/api/v1/friends/requests")
                        .with(csrf())
                        .with(user("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"david\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void testAcceptFriendRequest() throws Exception {
        Long pendingRequestId = friendshipRepository
                .findFriendshipBetweenUsers("bob", "alice")
                .orElseThrow(() -> new RuntimeException("Pending Request nicht gefunden"))
                .getId();
        mockMvc.perform(patch("/api/v1/friends/requests/{requestId}/accept", pendingRequestId)
                        .with(csrf())
                        .with(user("alice"))
                )
                .andExpect(status().isOk());
    }

    @Test
    void testDeclineFriendRequest() throws Exception {
        Friendship newRequest = new Friendship();
        newRequest.setUsername1("eve");
        newRequest.setUsername2("alice");
        newRequest.setStatus(FriendshipStatus.PENDING);
        newRequest.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        newRequest.setUpdatedAt(newRequest.getCreatedAt());
        friendshipRepository.save(newRequest);

        mockMvc.perform(patch("/api/v1/friends/requests/{requestId}/decline", newRequest.getId())
                        .with(csrf())
                        .with(user("alice"))
                )
                .andExpect(status().isOk());
    }

    @Test
    void testInviteToLobby() throws Exception {
        mockMvc.perform(post("/api/v1/friends/invite/{lobbyId}", "123")
                        .with(csrf())
                        .with(user("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetUsername\":\"bob\"}"))
                .andExpect(status().isOk());
    }
}
