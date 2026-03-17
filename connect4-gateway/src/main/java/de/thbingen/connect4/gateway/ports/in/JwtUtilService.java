package de.thbingen.connect4.gateway.ports.in;

import de.thbingen.connect4.gateway.model.enums.JwtTokenType;
import io.jsonwebtoken.Claims;

import java.util.Date;

public interface JwtUtilService {
    Claims getClaims(String token);

    String extractUserId(String token);

    String generate(Long userId, JwtTokenType tokenType);

    boolean validateToken(String jwt);

    boolean isAccessToken(String token);

    Date extractExpiration(String token);
}
