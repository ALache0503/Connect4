// ToDo needed and maybe extract Client usage into service
import api from "@/api/api.ts";
import {HttpStatusCode} from "axios";

export class MatchmakingService {
    static async startMatchmaking(): Promise<{ success: boolean; error?: string }> {
        const response = await api.post('/mm/register');

        if (response.status != HttpStatusCode.Ok) {
            return {success: false, error: response.statusText};
        }

        return {success: true};
    }
}