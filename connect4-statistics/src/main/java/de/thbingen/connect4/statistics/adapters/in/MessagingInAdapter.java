package de.thbingen.connect4.statistics.adapters.in;

import de.thbingen.connect4.common.model.dto.GameEventDto;
import de.thbingen.connect4.common.model.dto.UserEventDto;
import de.thbingen.connect4.common.ports.out.UserRepository;
import de.thbingen.connect4.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Controller;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
@Slf4j
@Controller
public class MessagingInAdapter {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final StatisticsService statisticsService;
    private final UserRepository userRepository;

    @RabbitListener(queues = "#{gameEventsQueue.name}")
    public void receive(String gameEventJson) {
        log.info("Received on game-events queue: {}", gameEventJson);
        GameEventDto gameEventDto = null;
        try {
            gameEventDto = objectMapper.readValue(gameEventJson, GameEventDto.class);
            log.info("Converted back: {}", gameEventDto.toString());

            processGameEvent(gameEventDto);

        } catch (IllegalArgumentException e) {
            log.error("Error processing game-event: " + gameEventJson, e);
            throw new RuntimeException(e);
        }
    }

    @RabbitListener(queues = "#{userEventsQueue.name}")
    public void receiveUserEvent(String userEventJson) {
        try {
            UserEventDto event = objectMapper.readValue(userEventJson, UserEventDto.class);
            log.info("Received event: {} for user ID: {}", event.getEventType(), event.getUserId());
            statisticsService.ensureStatisticsExist(event.getUserId());
        } catch (Exception e) {
            log.error("Error processing user-event: " + userEventJson, e);
            throw new RuntimeException(e);
        }
    }

    private void processGameEvent(GameEventDto gameEventDto) {
        switch (gameEventDto.getEventType()) {
            case WIN:
                log.info("Player {} won game", gameEventDto.getWinnerUsername());
                handleWinEvent(gameEventDto);
                break;
            case DRAW:
                log.info("Game between {} and {} is a draw",
                        gameEventDto.getPlayer1Username(), gameEventDto.getPlayer2Username());
                handleDrawEvent(gameEventDto);
                break;
            default:
                log.warn("Unknown event type");
        }
    }

    private void handleWinEvent(GameEventDto event) {
        Long winnerId = userRepository.getUser(event.getWinnerUsername());
        String loserUsername = event.getPlayer1Username().equals(event.getWinnerUsername())
                ? event.getPlayer2Username()
                : event.getPlayer1Username();

        Long loserId = userRepository.getUser(loserUsername);

        statisticsService.recordWin(winnerId, loserId);
    }

    private void handleDrawEvent(GameEventDto event) {
        Long player1Id = userRepository.getUser(event.getPlayer1Username());
        Long player2Id = userRepository.getUser(event.getPlayer2Username());
        statisticsService.recordDraw(player1Id, player2Id);
    }
}
