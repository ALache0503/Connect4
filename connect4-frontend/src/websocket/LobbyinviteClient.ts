import type {IMessage} from '@stomp/stompjs'
import {Client} from '@stomp/stompjs'

export interface LobbyInviteMessage {
    lobbyId: string;
    fromUsername: string;
    toUsername: string;
}

export class LobbyInviteClient {
    private client: Client;
    private connected = false;

    constructor(onInvite: (msg: LobbyInviteMessage) => void) {
        this.client = new Client({
            brokerURL: 'ws://localhost:8080/gaming/ws',
            reconnectDelay: 5000,
            heartbeatIncoming: 10000,
            heartbeatOutgoing: 10000,
            debug: (str) => console.log('[STOMP LobbyInvite]', str)
        });

        this.client.onConnect = () => {
            console.log('[STOMP LobbyInvite] Connected');
            this.connected = true;

            this.client.subscribe('/user/topic/lobby/invite', (message: IMessage) => {
                const payload = JSON.parse(message.body) as LobbyInviteMessage;
                console.log('[STOMP LobbyInvite] Invite received:', payload);
                onInvite(payload);
            });
        };

        this.client.onDisconnect = () => {
            console.log('[STOMP LobbyInvite] Disconnected');
            this.connected = false;
        };

        this.client.onStompError = (frame) => {
            console.error('[STOMP LobbyInvite] Error:', frame.headers['message'], frame.body);
        };

        this.client.activate();
    }

    isConnected(): boolean {
        return this.connected && this.client.connected;
    }

    async disconnect() {
        if (this.client.active) {
            this.client.reconnectDelay = 0;
            await this.client.deactivate();
            this.connected = false;
            console.log('[STOMP LobbyInvite] Deactivated');
        }
    }
}