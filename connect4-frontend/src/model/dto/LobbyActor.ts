import {type LobbyActorRole} from "@/model/dto/LobbyActorRole.ts";

export class LobbyActor {
    username: string;
    role: LobbyActorRole;

    constructor(username: string, role: LobbyActorRole) {
        this.username = username;
        this.role = role;
    }
}