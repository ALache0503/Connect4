package de.thbingen.connect4.statistics.adapters.out;

import de.thbingen.connect4.statistics.model.UserStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserStatisticsJpaRepository extends JpaRepository<UserStatistics, Long> {
    Optional<UserStatistics> findById(Long userId);

    boolean existsById(Long userId);

    @Modifying
    @Query("UPDATE UserStatistics u SET u.wins = u.wins + 1, u.updatedAt = CURRENT_TIMESTAMP WHERE u.userId = :userId")
    void incrementWins(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE UserStatistics u SET u.losses = u.losses + 1, u.updatedAt = CURRENT_TIMESTAMP WHERE u.userId = :userId")
    void incrementLosses(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE UserStatistics u SET u.draws = u.draws + 1, u.updatedAt = CURRENT_TIMESTAMP WHERE u.userId = :userId")
    void incrementDraws(@Param("userId") Long userId);

    // Spring Data generates query
    Page<UserStatistics> findAllByOrderByWinsDesc(Pageable pageable);
}
