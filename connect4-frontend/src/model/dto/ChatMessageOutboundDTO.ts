import {type ChatType} from "@/model/enum/ChatType.ts";

export class ChatMessageOutboundDTO {
    targetId: string;
    text: string;
    author: string;
    createdAt: number;
    type: ChatType;

    constructor(targetId: string, text: string, author: string, createdAt: number, type: ChatType) {
        this.targetId = targetId;
        this.text = text;
        this.author = author;
        this.createdAt = createdAt;
        this.type = type;
    }
}