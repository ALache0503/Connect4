import type {IMessage} from '@stomp/stompjs'
import {Client} from '@stomp/stompjs'

export interface FriendUpdateMessage {
    type: 'REQUEST_RECEIVED' | 'REQUEST_ACCEPTED' | 'REQUEST_DECLINED' | 'FRIEND_REMOVED';
    friendshipId: number;
    fromUsername: string;
    toUsername: string;
}

export interface OnlineStatusMessage {
    username: string;
    online: boolean;
}

export interface LobbyInviteMessage {
    lobbyId: string;
    fromUsername: string;
    toUsername: string;
}

export class FriendlistClient {
    private client: Client;
    private connected: boolean = false;

    constructor(
        onFriendUpdate: (msg: FriendUpdateMessage) => void,
        onStatusUpdate: (msg: OnlineStatusMessage) => void,
        onLobbyInvite: (msg: LobbyInviteMessage) => void
    ) {
        this.client = new Client({
            brokerURL: 'ws://localhost:8080/friends/ws',
            reconnectDelay: 5000,
            heartbeatIncoming: 10000,
            heartbeatOutgoing: 10000,
            debug: (str) => console.log('[STOMP Friends]', str)
        });

        this.client.onConnect = () => {
            console.log('[STOMP Friends] Connected');
            this.connected = true;

            this.client.subscribe('/user/topic/friends', (message: IMessage) => {
                const payload = JSON.parse(message.body) as FriendUpdateMessage;
                console.log('[STOMP Friends] Friend update:', payload);
                onFriendUpdate(payload);
            });

            this.client.subscribe('/user/topic/friends/status', (message: IMessage) => {
                const payload = JSON.parse(message.body) as OnlineStatusMessage;
                console.log('[STOMP Friends] Status update:', payload);
                onStatusUpdate(payload);
            });

            this.client.subscribe('/user/topic/lobby/invite', (message: IMessage) => {
                const payload = JSON.parse(message.body) as LobbyInviteMessage;
                console.log('[STOMP LobbyInvite] Invite received:', payload);
                onLobbyInvite(payload);
            });
        };

        this.client.onDisconnect = () => {
            console.log('[STOMP Friends] Disconnected');
            this.connected = false;
        };

        this.client.onStompError = (frame) => {
            console.error('[STOMP Friends] Error:', frame.headers['message'], frame.body);
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
            console.log('[STOMP Friends] Deactivated');
        }
    }
}