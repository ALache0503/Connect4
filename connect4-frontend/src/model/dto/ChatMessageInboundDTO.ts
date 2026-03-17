import {type ChatType} from "@/model/enum/ChatType.ts";

export class ChatMessageInboundDTO {
    text: string;
    chatType: ChatType;

    constructor(text: string, chatType: ChatType) {
        this.text = text;
        this.chatType = chatType;
    }
}