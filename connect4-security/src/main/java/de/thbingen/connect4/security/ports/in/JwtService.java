package de.thbingen.connect4.security.ports.in;

import de.thbingen.connect4.common.model.enums.JwtTokenType;

public interface JwtService {
    String generate(Long userId, JwtTokenType tokenType);
}
