import api from "@/api/api.ts";
import type {LobbyDTO} from "@/model/dto/LobbyDTO.ts";
import type {LobbyState} from "@/model/enum/LobbyState.ts";
import {HttpStatusCode} from "axios";
import type {CreateLobbyResponse} from "@/model/dto/CreateLobbyResponse.ts";
import {LobbyActorRole} from "@/model/dto/LobbyActorRole.ts";

export class LobbyService {
    static async createLobby(): Promise<{ success: boolean; error?: string; lobbyId?: string }> {
        try {
            const lobbyResponse = (await api.post<CreateLobbyResponse>("/lobby/create")).data;

            console.log(lobbyResponse);

            return {success: true, lobbyId: lobbyResponse.lobbyId};
        } catch (error: any) {
            if (error.response) {
                const msg = error.response.data?.message || "Unbekannter Fehler";

                return {success: false, error: msg};
            }

            console.log(error);

            return {success: false, error: "Service offline"};
        }
    }

    static async joinLobby(lobbyId: string, role: LobbyActorRole): Promise<{ success: boolean; error?: string; }> {
        if (!lobbyId.trim()) {
            return {success: false, error: "Bitte gib einen Lobby-Namen ein!"};
        }

        try {
            const response = await api.post<LobbyDTO>(`/lobby/join/${lobbyId}`, role);

            if (response.status !== HttpStatusCode.Ok) {
                return {success: false, error: "Lobby nicht gefunden"}
            }

            return {success: true};
        } catch (error: any) {
            return {success: false, error: "Service offline"}
        }
    }

    static async getLobby(lobbyId: string): Promise<{ success: boolean; lobbyDto?: LobbyDTO; error?: string; }> {
        if (!lobbyId.trim()) {
            return {success: false, error: "Bitte gib einen Lobby-Namen ein!"};
        }

        try {
            const lobbyDto = (await api.get<LobbyDTO>(`/lobby/${lobbyId}`)).data;

            return {success: true, lobbyDto: lobbyDto};
        } catch (error: any) {
            return {success: false, error: "Service offline"};
        }
    }

    static async setState(lobbyId: string, newState: LobbyState): Promise<{ success: boolean; error?: string; }> {
        if (!lobbyId.trim()) {
            return {success: false, error: "Bitte gib einen Lobby-Namen ein!"};
        }

        try {
            const response = (await api.patch(`/lobby/state/${lobbyId}`, newState));

            if (response.status === HttpStatusCode.Ok) {
                return {success: true}
            }
        } catch (error: any) {
            return {success: false, error: "Service offline"};
        }

        return {success: false, error: "Service fehler"};
    }

    static async setRole(lobbyId: string, newRole: LobbyActorRole): Promise<{
        success: boolean;
        error?: string;
    }> {
        if (!lobbyId.trim()) {
            return {success: false, error: "Bitte gib einen Lobby-Namen ein!"};
        }

        try {
            const response = (await api.patch(`/lobby/role/${lobbyId}`, newRole));

            if (response.status === HttpStatusCode.Ok) {
                return {success: true}
            }
        } catch (error: any) {
            return {success: false, error: "Service offline"};
        }

        return {success: false, error: "Service fehler"};
    }
}
