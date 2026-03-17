import mitt from "mitt";
import type {ChatType} from "@/model/enum/ChatType";

type Events = {
    "chat:openChannel": { chatType: ChatType; targetId: string };
    "chat:closeChannel": { chatType: ChatType; targetId: string };
};

export const eventBus = mitt<Events>();
