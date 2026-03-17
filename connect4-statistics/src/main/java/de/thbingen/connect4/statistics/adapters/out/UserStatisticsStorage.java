package de.thbingen.connect4.statistics.adapters.out;

import de.thbingen.connect4.statistics.model.UserStatistics;
import de.thbingen.connect4.statistics.ports.out.UserStatisticsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserStatisticsStorage implements UserStatisticsPort {

    private final UserStatisticsJpaRepository jpaRepository;

    @Override
    public Optional<UserStatistics> findById(Long userId) {
        return jpaRepository.findById(userId);
    }

    @Override
    public UserStatistics save(UserStatistics statistics) {
        return jpaRepository.save(statistics);
    }

    @Override
    public boolean existsById(Long userId) {
        return jpaRepository.existsById(userId);
    }

    @Override
    public void incrementWins(Long userId) {
        jpaRepository.incrementWins(userId);
    }

    @Override
    public void incrementLosses(Long userId) {
        jpaRepository.incrementLosses(userId);
    }

    @Override
    public void incrementDraws(Long userId) {
        jpaRepository.incrementDraws(userId);
    }

    @Override
    public Page<UserStatistics> findAllOrderByWinsDesc(Pageable pageable) {
        return jpaRepository.findAllByOrderByWinsDesc(pageable);
    }
}
