package de.thbingen.connect4.gaming.adapters.out;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.thbingen.connect4.common.model.dto.GameEventDto;
import de.thbingen.connect4.gaming.ports.out.MessagingOutPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MessagingOutAdapter implements MessagingOutPort {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    AmqpTemplate amqpTemplate;

    @Autowired
    private Exchange gameExchange;

    @Override
    public void sendGameEvent(GameEventDto gameEventDto) {
        try {
            String messageJson = objectMapper.writeValueAsString(gameEventDto);

            String routingKey = "game." + gameEventDto.getEventType().toString().toLowerCase();

            amqpTemplate.convertAndSend(
                    gameExchange.getName(),
                    routingKey,
                    messageJson
            );

            log.info("Sent GAME_EVENT for game between {} and {}, eventType: {}",
                    gameEventDto.getPlayer1Username(),
                    gameEventDto.getPlayer2Username(),
                    gameEventDto.getEventType());

        } catch (JsonProcessingException e) {
            log.error("Error sending game event", e);
            throw new RuntimeException(e);
        }
    }
}
