import type {LobbyState} from "@/model/enum/LobbyState.ts";
import type {LobbyActor} from "@/model/dto/LobbyActor.ts";

export class LobbyDTO {
    id: string;
    actors: LobbyActor[];
    state: LobbyState;

    constructor(
        id: string,
        actors: LobbyActor[],
        state: LobbyState
    ) {
        this.id = id;
        this.state = state;
        this.actors = actors;
    }
}