import type {IMessage} from "@stomp/stompjs";
import {Client} from '@stomp/stompjs'
import {MatchmakingService} from "@/service/MatchmakingService.ts";

export class MatchmakingClient {
    private client: Client;

    constructor(onMessage: (msg: any) => void, onError: (msg: any) => void) {
        this.client = new Client({
            brokerURL: `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/mm/ws`,
            reconnectDelay: 5000,
            heartbeatIncoming: 10000,
            heartbeatOutgoing: 10000,
            debug: (str) => console.log('[STOMP]', str)
        });

        this.client.onConnect = async () => {
            this.client.subscribe('/user/topic/mm', (message: IMessage) => {
                const payload = JSON.parse(message.body);
                onMessage(payload);
            });

            const response = await MatchmakingService.startMatchmaking();

            if (!response.success) {
                onError(response.error);
            }
        };

        this.client.onStompError = (frame) => {
            console.error('Broker reported error: ', frame.headers['message'], frame.body)
        };

        this.client.activate();
    }

    send(destination: string, body: any) {
        if (this.client.connected) {
            this.client.publish({destination, body: JSON.stringify(body)});
        }
    }

    disconnect() {
        if (this.client.active) this.client.deactivate().then(() => {
        });
    }
}