package de.thbingen.connect4.statistics.ports.out;

import de.thbingen.connect4.statistics.model.UserStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserStatisticsPort {
    Optional<UserStatistics> findById(Long userId);
    UserStatistics save(UserStatistics statistics);
    boolean existsById(Long userId);
    void incrementWins(Long userId);
    void incrementLosses(Long userId);
    void incrementDraws(Long userId);

    Page<UserStatistics> findAllOrderByWinsDesc(Pageable pageable);
}
