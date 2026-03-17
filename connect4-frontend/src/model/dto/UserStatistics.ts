export interface UserStatistics {
    username: string;
    totalGames: number;
    wins: number;
    losses: number;
    draws?: number;
    winRate?: number;
}