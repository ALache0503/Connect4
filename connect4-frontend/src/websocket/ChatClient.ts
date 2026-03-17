import type {IMessage} from '@stomp/stompjs'
import {Client} from '@stomp/stompjs'
import {ChatMessageInboundDTO} from "@/model/dto/ChatMessageInboundDTO.ts";
import type {ChatMessageOutboundDTO} from "@/model/dto/ChatMessageOutboundDTO.ts";
import {ChatType} from "@/model/enum/ChatType.ts";

export class ChatClient {
    private client: Client;
    private onMessage: (msg: ChatMessageOutboundDTO) => void;

    constructor(onMessage: (msg: any) => void) {
        this.onMessage = onMessage;

        this.client = new Client({
            brokerURL: `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/chat/ws`,
            reconnectDelay: 5000,
            heartbeatIncoming: 10000,
            heartbeatOutgoing: 10000,
            debug: (str) => console.log('[STOMP-CHAT]', str)
        });

        this.client.onConnect = () => {
            this.client.subscribe('/topic/chat/global', (message: IMessage) => {
                const payload: ChatMessageOutboundDTO = JSON.parse(message.body);

                onMessage(payload);
            });
        };

        this.client.onStompError = (frame) => {
            console.error('Broker reported error: ', frame.headers['message'], frame.body);
        };

        this.client.activate();
    }

    subscribe(targetId: string, chatType: ChatType) {
        if (this.client.connected) {
            switch (chatType) {
                case ChatType.DM: {
                    // TODO wait for friendlist

                    break;
                }
                case ChatType.LOBBY: {
                    this.client.subscribe(`/topic/chat/lobby/${targetId}`, (message: IMessage) => {
                        const payload: ChatMessageOutboundDTO = JSON.parse(message.body);

                        this.onMessage(payload);
                    });

                    break;
                }
            }
        }
    }

    unsubscribe(targetId: string, chatType: ChatType) {
        if (this.client.connected) {
            switch (chatType) {
                case ChatType.DM: {
                    // TODO wait for friendlist

                    break;
                }
                case ChatType.LOBBY: {
                    this.client.unsubscribe(`/topic/chat/lobby/${targetId}`);

                    break;
                }
            }
        }
    }

    send(targetId: string, text: string, chatType: ChatType) {
        if (this.client.connected) {
            const dto = new ChatMessageInboundDTO(text, chatType);

            this.client.publish({destination: `/app/chat.sendMessage/${targetId}`, body: JSON.stringify(dto)});
        }
    }

    async disconnect() {
        if (this.client.active) {
            this.client.reconnectDelay = 0;
            await this.client.deactivate();
        }
    }
}
