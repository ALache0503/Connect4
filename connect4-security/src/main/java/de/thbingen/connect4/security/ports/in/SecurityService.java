package de.thbingen.connect4.security.ports.in;

import de.thbingen.connect4.common.model.dto.AuthRequest;
import de.thbingen.connect4.common.model.dto.AuthResponse;

public interface SecurityService {

    AuthResponse register(AuthRequest request);

    AuthResponse login(AuthRequest request);

    AuthResponse refresh(String token);

}
