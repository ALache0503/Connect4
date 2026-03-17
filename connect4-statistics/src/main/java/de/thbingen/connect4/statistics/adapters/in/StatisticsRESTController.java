package de.thbingen.connect4.statistics.adapters.in;

import de.thbingen.connect4.common.ports.out.UserRepository;
import de.thbingen.connect4.statistics.model.UserStatisticsDto;
import de.thbingen.connect4.statistics.ports.in.StatisticsRESTPortInterf;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
@Slf4j
public class StatisticsRESTController {

    private final StatisticsRESTPortInterf statisticsService;

    private final UserRepository userRepository;

    // returns stats for specific user
    @GetMapping("/{username}")
    public ResponseEntity<UserStatisticsDto> getUserStatistics(@PathVariable String username) {
        log.info("Getting userId for username {}", username);

        Long userId = userRepository.getUser(username);
        log.info("GET request for user statistics: userId={}", userId);

        return statisticsService.getUserStatistics(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/statistics/leaderboard
     * returns leaderboard with pageination, sorted by wins
     * <p>
     * Query Parameters:
     * page: Page number (0-indexed, default: 0)
     * size: Page size (default: 10)
     * <p>
     * Example: /api/v1/statistics/leaderboard?page=0&size=20
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<Page<UserStatisticsDto>> getLeaderboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("GET request for leaderboard: page={}, size={}", page, size);

        // Validierung
        if (page < 0) page = 0;
        if (size < 1 || size > 100) size = 10;

        Pageable pageable = PageRequest.of(page, size);
        Page<UserStatisticsDto> leaderboard = statisticsService.getLeaderboard(pageable);

        return ResponseEntity.ok(leaderboard);
    }
}