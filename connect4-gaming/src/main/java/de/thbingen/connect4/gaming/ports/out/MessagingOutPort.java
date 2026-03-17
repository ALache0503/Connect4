package de.thbingen.connect4.gaming.ports.out;

import de.thbingen.connect4.common.model.dto.GameEventDto;

public interface MessagingOutPort {
    void sendGameEvent(GameEventDto gameEventDto);
}
