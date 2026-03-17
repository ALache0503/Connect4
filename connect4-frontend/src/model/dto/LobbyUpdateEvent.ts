import type {LobbyUpdateType} from "@/model/enum/LobbyUpdateType.ts";
import type {LobbyDTO} from "@/model/dto/LobbyDTO.ts";

export class LobbyUpdateEvent {
    lobby: LobbyDTO;
    type: LobbyUpdateType;
    target: string;

    constructor(
        lobby: LobbyDTO,
        type: LobbyUpdateType,
        target: string
    ) {
        this.lobby = lobby;
        this.type = type;
        this.target = target;
    }
}