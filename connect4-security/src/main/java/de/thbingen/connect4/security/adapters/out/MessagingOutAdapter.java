package de.thbingen.connect4.security.adapters.out;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.thbingen.connect4.common.model.dto.UserEventDto;
import de.thbingen.connect4.security.ports.out.MessagingOutPort;
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
    private Exchange securityExchange;

    @Override
    public void sendUserCreatedEvent(Long userId) {
        UserEventDto userEventDto = new UserEventDto(userId, "USER_CREATED");

        try {
            String messageJson = objectMapper.writeValueAsString(userEventDto);
            amqpTemplate.convertAndSend(
                    securityExchange.getName(),
                    "user.created",
                    messageJson
            );
            log.info("Sent USER_CREATED event for userId: {}", userId);
        } catch (JsonProcessingException e) {
            log.error("Error sending user event", e);
            throw new RuntimeException(e);
        }
    }
}
