package de.thbingen.connect4.statistics.service;

import de.thbingen.connect4.common.ports.out.UserRepository;
import de.thbingen.connect4.statistics.model.UserStatistics;
import de.thbingen.connect4.statistics.model.UserStatisticsDto;
import de.thbingen.connect4.statistics.ports.in.StatisticsRESTPortInterf;
import de.thbingen.connect4.statistics.ports.out.UserStatisticsPort;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatisticsService implements StatisticsRESTPortInterf {

    private final UserStatisticsPort userStatisticsPort;
    private final UserRepository userRepository;

    // RabbitMQ Event Handling
    @Transactional
    public void recordWin(Long winnerId, Long loserId) {

        userStatisticsPort.incrementWins(winnerId);
        userStatisticsPort.incrementLosses(loserId);

        log.info("Recorded win for user {} and loss for user {}", winnerId, loserId);
    }

    @Transactional
    public void recordDraw(Long player1Id, Long player2Id) {

        userStatisticsPort.incrementDraws(player1Id);
        userStatisticsPort.incrementDraws(player2Id);

        log.info("Recorded draw for users {} and {}", player1Id, player2Id);
    }

    public void ensureStatisticsExist(Long userId) {
        if (!userStatisticsPort.existsById(userId)) {
            UserStatistics stats = new UserStatistics();
            stats.setUserId(userId);
            stats.setWins(0);
            stats.setLosses(0);
            stats.setDraws(0);
            stats.setUpdatedAt(LocalDateTime.now());
            userStatisticsPort.save(stats);
            log.debug("Created new statistics entry for user {}", userId);
        }
    }


    // REST Use Cases
    @Override
    public Optional<UserStatisticsDto> getUserStatistics(Long userId) {
        log.info("Fetching statistics for user {}", userId);
        return userStatisticsPort.findById(userId)
                .map(UserStatisticsDto::fromEntity);
    }

    @Override
    public Page<UserStatisticsDto> getLeaderboard(Pageable pageable) {
        log.info("Fetching leaderboard page {} with size {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<UserStatistics> leaderboard =
                userStatisticsPort.findAllOrderByWinsDesc(pageable);

        List<Long> userIds = leaderboard.getContent().stream()
                .map(UserStatistics::getUserId)
                .toList();

        Map<Long, String> usernameMap = userRepository.getUsernamesByIds(userIds);

        return leaderboard.map(entity -> {
            UserStatisticsDto dto = UserStatisticsDto.fromEntity(entity);
            dto.setUsername(usernameMap.getOrDefault(entity.getUserId(), "Unknown"));
            return dto;
        });
    }
}