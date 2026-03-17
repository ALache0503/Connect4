package de.thbingen.connect4.common.ports.in;

import io.jsonwebtoken.Claims;

import java.util.Date;

public interface JwtUtilService {
    Claims getClaims(String token);

    String extractUserId(String token);

    boolean validateToken(String jwt);

    boolean isAccessToken(String token);

    Date extractExpiration(String token);
}
