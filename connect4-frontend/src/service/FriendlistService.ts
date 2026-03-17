import api from "@/api/api.ts";

export interface FriendEntry {
    id: string;
    username: string;
    online: boolean;
}

export interface FriendRequestEntry {
    id: string;
    username: string;
}

const normalizeFriend = (entry: any): FriendEntry => {
    const id = entry?.id ?? entry?.userId ?? entry?.friendId ?? entry?.friend?.id ?? entry?.targetId ?? '';
    const username = entry?.username ?? entry?.name ?? entry?.friend?.username ?? entry?.user?.username ?? entry?.targetUsername ?? '';
    const online = entry?.online ?? false;

    return {
        id: String(id ?? ''),
        username: String(username ?? ''),
        online: Boolean(online)
    };
};

const normalizeRequest = (entry: any): FriendRequestEntry => {
    const id = entry?.id ?? entry?.requestId ?? entry?.friendshipId ?? '';
    const username = entry?.fromUser?.username ?? entry?.fromUsername ?? entry?.requester?.username ?? entry?.user?.username ?? entry?.username ?? '';

    return {
        id: String(id ?? ''),
        username: String(username ?? '')
    };
};

const formatError = (error: any, fallback: string): string => {
    const response = error?.response;
    if (response) {
        const data = response.data;
        const message = data?.message || data?.error;

        if (typeof message === 'string' && message.trim()) {
            return message;
        }

        if (typeof data === 'string' && data.trim()) {
            return data;
        }

        if (typeof response.status === 'number') {
            return `Anfrage fehlgeschlagen (HTTP ${response.status})`;
        }
    }

    return error?.message || fallback;
};

export class FriendlistService {
    static async getFriends(): Promise<{ success: boolean; friends: FriendEntry[]; error?: string }> {
        try {
            const response = await api.get('/friends');
            const data = response.data;
            const list: unknown[] = Array.isArray(data) ? data : Array.isArray(data?.friends) ? data.friends : [];
            const friends = list.map((entry: unknown) => normalizeFriend(entry))
                .filter((friend: FriendEntry) => friend.username);

            return {
                success: true,
                friends
            };
        } catch (error: any) {
            return {
                success: false,
                friends: [],
                error: formatError(error, 'Service offline')
            };
        }
    }

    static async getRequests(): Promise<{
        success: boolean;
        incoming: FriendRequestEntry[];
        outgoing: FriendRequestEntry[];
        error?: string;
    }> {
        try {
            const response = await api.get('/friends/requests');
            const data = response.data;
            let incomingRaw: any[] = [];
            let outgoingRaw: any[] = [];

            if (Array.isArray(data)) {
                incomingRaw = data;
            } else {
                incomingRaw = Array.isArray(data?.incoming) ? data.incoming : Array.isArray(data?.incomingRequests) ? data.incomingRequests : [];
                outgoingRaw = Array.isArray(data?.outgoing) ? data.outgoing : Array.isArray(data?.outgoingRequests) ? data.outgoingRequests : [];

                if (!incomingRaw.length && !outgoingRaw.length && Array.isArray(data?.requests)) {
                    incomingRaw = data.requests;
                }
            }

            const incoming = incomingRaw.map((entry: unknown) => normalizeRequest(entry))
                .filter((request: FriendRequestEntry) => request.username);
            const outgoing = outgoingRaw.map((entry: unknown) => normalizeRequest(entry))
                .filter((request: FriendRequestEntry) => request.username);

            return {
                success: true,
                incoming,
                outgoing
            };
        } catch (error: any) {
            return {
                success: false,
                incoming: [],
                outgoing: [],
                error: formatError(error, 'Service offline')
            };
        }
    }

    static async sendRequest(targetUsername: string): Promise<{ success: boolean; error?: string }> {
        if (!targetUsername.trim()) {
            return {success: false, error: 'Benutzername erforderlich'};
        }

        try {
            const response = await api.post(`/friends/requests`, {
                username: targetUsername
            });

            if (response.status >= 200 && response.status < 300) {
                return {success: true};
            }

            return {success: false, error: 'Request failed'};
        } catch (error: any) {
            return {
                success: false,
                error: formatError(error, 'Service offline')
            };
        }
    }

    static async acceptRequest(requestId: string): Promise<{ success: boolean; error?: string }> {
        if (!requestId.trim()) {
            return {success: false, error: 'Request ID required'};
        }

        try {
            const response = await api.patch(`/friends/requests/${requestId}/accept`);

            if (response.status >= 200 && response.status < 300) {
                return {success: true};
            }

            return {success: false, error: 'Request failed'};
        } catch (error: any) {
            return {
                success: false,
                error: formatError(error, 'Service offline')
            };
        }
    }

    static async inviteToLobby(lobbyId: string, username: string): Promise<{ success: boolean; error?: string }> {
        if (!lobbyId.trim()) {
            return {success: false, error: 'Lobby ID required'};
        }

        if (!username.trim()) {
            return {success: false, error: 'Username required'};
        }

        try {
            const response = await api.post(`/friends/invite/${lobbyId}`, {
                targetUsername: username
            });

            if (response.status >= 200 && response.status < 300) {
                return {success: true};
            }

            return {success: false, error: 'Invite failed'};
        } catch (error: any) {
            return {
                success: false,
                error: formatError(error, 'Service offline')
            };
        }
    }
}
