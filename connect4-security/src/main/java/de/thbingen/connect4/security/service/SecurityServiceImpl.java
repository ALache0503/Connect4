package de.thbingen.connect4.security.service;

import de.thbingen.connect4.common.exception.UsernameTakenException;
import de.thbingen.connect4.common.model.dto.AuthRequest;
import de.thbingen.connect4.common.model.dto.AuthResponse;
import de.thbingen.connect4.common.model.enums.JwtTokenType;
import de.thbingen.connect4.common.ports.in.JwtUtilService;
import de.thbingen.connect4.security.exception.LoginFailedException;
import de.thbingen.connect4.security.model.entity.User;
import de.thbingen.connect4.security.ports.in.JwtService;
import de.thbingen.connect4.security.ports.in.SecurityService;
import de.thbingen.connect4.security.ports.in.UserSecurityService;
import de.thbingen.connect4.security.ports.out.MessagingOutPort;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;

@Service
@Log4j2
@RequiredArgsConstructor
public class SecurityServiceImpl implements SecurityService {
    private final JwtUtilService jwtUtilService;
    private final JwtService jwtService;
    private final UserSecurityService userSecurityService;
    private final MessagingOutPort messagingOutPort;

    @Override
    public AuthResponse register(AuthRequest request) {
        if (userSecurityService.usernameExists(request.getUsername())) {
            throw new UsernameTakenException();
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt(12)))
                .updatedAt(Timestamp.from(Instant.now()))
                .createdAt(Timestamp.from(Instant.now()))
                .build();

        user = userSecurityService.createUser(user);

        messagingOutPort.sendUserCreatedEvent(user.getId());

        String accessToken = jwtService.generate(user.getId(), JwtTokenType.ACCESS);
        String refreshToken = jwtService.generate(user.getId(), JwtTokenType.REFRESH);

        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        User user = userSecurityService.getUserByUsername(request.getUsername()).orElseThrow(LoginFailedException::new);
        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) throw new LoginFailedException();

        String accessToken = jwtService.generate(user.getId(), JwtTokenType.ACCESS);
        String refreshToken = jwtService.generate(user.getId(), JwtTokenType.REFRESH);

        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        Claims claims = jwtUtilService.getClaims(refreshToken);

        Long id = Long.valueOf(claims.getSubject());

        User user = userSecurityService.getUserById(id).orElseThrow(LoginFailedException::new);

        String accessToken = jwtService.generate(user.getId(), JwtTokenType.ACCESS);
        refreshToken = jwtService.generate(user.getId(), JwtTokenType.REFRESH);

        return new AuthResponse(accessToken, refreshToken);
    }
}
