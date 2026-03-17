import type {IMessage} from '@stomp/stompjs'
import {Client} from '@stomp/stompjs'

export class LobbyClient {
    private client: Client;

    constructor(lobbyId: string, onLobbyMessage: (msg: any) => void, onGameMessage: (msg: any) => void) {


        this.client = new Client({
            brokerURL: `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/gaming/ws`,
            reconnectDelay: 5000,
            heartbeatIncoming: 10000,
            heartbeatOutgoing: 10000,
            debug: (str) => console.log('[STOMP]', str)
        });

        this.client.onConnect = () => {
            console.log('[STOMP] Connected');

            this.client.subscribe(`/user/topic/lobby/${lobbyId}`, (message: IMessage) => {
                const payload = JSON.parse(message.body);
                onLobbyMessage(payload);
            });
            this.client.subscribe(`/user/topic/game/${lobbyId}`, (message: IMessage) => {
                console.log('Game Event received:', message.body);
                onGameMessage(JSON.parse(message.body));
            });
        };

        this.client.onStompError = (frame) => {
            console.error('Broker reported error: ', frame.headers['message'], frame.body);
        };

        this.client.activate();
    }

    async disconnect() {
        if (this.client.active) {
            this.client.reconnectDelay = 0;
            await this.client.deactivate();
        }
    }
}
