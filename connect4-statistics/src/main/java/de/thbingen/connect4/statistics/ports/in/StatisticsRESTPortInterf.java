package de.thbingen.connect4.statistics.ports.in;

import de.thbingen.connect4.statistics.model.UserStatisticsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface StatisticsRESTPortInterf {

    // Get stats for specific user
    Optional<UserStatisticsDto> getUserStatistics(Long userId);

    // Get paginated leaderboard sorted by wins
    Page<UserStatisticsDto> getLeaderboard(Pageable pageable);
}
