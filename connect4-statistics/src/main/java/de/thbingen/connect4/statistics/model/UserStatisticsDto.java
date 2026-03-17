package de.thbingen.connect4.statistics.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsDto {
    private Long userId;
    private String username;
    private Integer wins;
    private Integer losses;
    private Integer draws;
    private Integer totalGames;
    private Double winRate;

    public static UserStatisticsDto fromEntity(UserStatistics entity) {
        UserStatisticsDto dto = new UserStatisticsDto();
        dto.setUserId(entity.getUserId());
        dto.setWins(entity.getWins());
        dto.setLosses(entity.getLosses());
        dto.setDraws(entity.getDraws());

        int total = entity.getWins() + entity.getLosses() + entity.getDraws();
        dto.setTotalGames(total);
        dto.setWinRate(total > 0 ? (double) entity.getWins() / total * 100 : 0.0);

        return dto;
    }
}