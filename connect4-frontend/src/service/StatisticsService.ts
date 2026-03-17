import axios from 'axios';
import type {UserStatistics} from '@/model/dto/UserStatistics';
import type {Page} from '@/types/Page';

const API_BASE_URL = 'http://localhost:8080/api/v1/statistics';

interface ApiResponse<T> {
    success: boolean;
    data?: T;
    error?: string;
}

export class StatisticsService {
    /**
     * Lädt die Statistiken eines bestimmten Benutzers anhand des Usernamens
     */
    static async getUserStatistics(username: string): Promise<ApiResponse<UserStatistics>> {
        try {
            const response = await axios.get<UserStatistics>(`${API_BASE_URL}/${username}`, {
                headers: {
                    'Content-Type': 'application/json',
                },
                withCredentials: true,
            });

            return {
                success: true,
                data: response.data,
            };
        } catch (error: any) {
            console.error('Error fetching user statistics:', error);
            return {
                success: false,
                error: error.response?.data?.message || 'Fehler beim Laden der Benutzerstatistiken',
            };
        }
    }

    /**
     * Lädt das Leaderboard mit allen Spielerstatistiken
     */
    static async getLeaderboard(): Promise<ApiResponse<UserStatistics[]>> {
        try {
            const response = await axios.get<Page<UserStatistics>>(`${API_BASE_URL}/leaderboard`, {
                headers: {
                    'Content-Type': 'application/json',
                },
                withCredentials: true,
            });

            return {
                success: true,
                data: response.data.content,
            };
        } catch (error: any) {
            console.error('Error fetching leaderboard:', error);
            return {
                success: false,
                error: error.response?.data?.message || 'Fehler beim Laden des Leaderboards',
            };
        }
    }
}