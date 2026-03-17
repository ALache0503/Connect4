package de.thbingen.connect4.common.model.dto;

import de.thbingen.connect4.common.model.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class GameEventDto {
    private EventType eventType;
    private String winnerUsername;
    private String player1Username;
    private String player2Username;
}
