<template>
  <div class="friendlist-section" data-bs-theme="dark">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div class="d-flex align-items-center gap-2">
        <h4 class="mb-0">Freundesliste</h4>
        <span :class="['connection-dot', wsConnected ? 'online' : 'offline']"
              :title="wsConnected ? 'Verbunden' : 'Nicht verbunden'"></span>
      </div>

    </div>

    <div v-if="errorText" class="alert alert-danger alert-sm py-2" role="alert">
      {{ errorText }}
    </div>

    <div class="friendlist-grid">
      <div class="friend-card p-3">
        <h6 class="text-light">Freunde</h6>
        <div v-if="loading" class="text-muted small">Laden...</div>
        <div v-else-if="friends.length === 0" class="text-muted small">Noch keine Freunde</div>
        <ul v-else class="list-group list-group-flush friend-list">
          <li v-for="friend in friends"
              :key="friend.id || friend.username"
              class="list-group-item d-flex align-items-center gap-2">
            <span :class="['status-dot', friend.online ? 'online' : 'offline']"
                  :title="friend.online ? 'Online' : 'Offline'"></span>
            <span class="flex-grow-1">{{ friend.username }}</span>
            <button
                :disabled="!canInvite || inviting"
                class="btn btn-sm btn-outline-primary"
                @click="inviteFriend(friend)">
              Einladen
            </button>
          </li>
        </ul>
        <div v-if="!canInvite && friends.length > 0" class="text-muted small mt-2">
          Tritt einer Lobby bei, um Freunde einzuladen
        </div>
      </div>

      <div class="friend-card p-3">
        <h6 class="text-light">Anfragen</h6>
        <div v-if="loading" class="text-muted small">Laden...</div>
        <div v-else>
          <div v-if="incomingRequests.length === 0" class="text-muted small">
            Keine eingehenden Anfragen
          </div>
          <ul v-else class="list-group list-group-flush friend-list">
            <li v-for="request in incomingRequests"
                :key="request.id || request.username"
                class="list-group-item d-flex justify-content-between align-items-center">
              <span>{{ request.username }}</span>
              <button
                  :disabled="!request.id"
                  class="btn btn-sm btn-success"
                  @click="acceptRequest(request.id)">
                Annehmen
              </button>
            </li>
          </ul>

          <div class="mt-3">
            <div class="text-muted small mb-2">Ausstehend</div>
            <div v-if="outgoingRequests.length === 0" class="text-muted small">
              Keine ausstehenden Anfragen
            </div>
            <ul v-else class="list-group list-group-flush friend-list">
              <li v-for="request in outgoingRequests"
                  :key="request.id || request.username"
                  class="list-group-item">
                {{ request.username }}
              </li>
            </ul>
          </div>

          <div class="mt-3">
            <div class="text-muted small mb-2">Einladungen</div>
            <div v-if="lobbyInvites.length === 0" class="text-muted small">
              Keine Einladungen
            </div>
            <ul v-else class="list-group list-group-flush friend-list">
              <li v-for="invite in lobbyInvites"
                  :key="`${invite.lobbyId}-${invite.fromUsername}`"
                  class="list-group-item d-flex justify-content-between align-items-center">
                <span>{{ invite.fromUsername }}: {{ invite.lobbyId }}</span>
                <button
                    :disabled="joiningInvite"
                    class="btn btn-sm btn-primary"
                    @click="joinInvite(invite)">
                  Beitreten
                </button>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <div class="friend-card p-3">
        <h6 class="text-light">Neue Anfrage</h6>
        <p class="text-muted small mb-2">Sende eine Anfrage per Benutzername</p>
        <div class="input-group">
          <input
              v-model="newFriendTarget"
              class="form-control"
              placeholder="Benutzername"
              type="text"
              @keyup.enter="sendRequest"
          />
          <button :disabled="sending" class="btn btn-primary" @click="sendRequest">
            Senden
          </button>
        </div>
        <div v-if="infoText" class="text-success small mt-2">{{ infoText }}</div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, onUnmounted, ref} from 'vue';
import router from '@/router';
import {type FriendEntry, FriendlistService, type FriendRequestEntry} from '@/service/FriendlistService';
import {
  FriendlistClient,
  type FriendUpdateMessage,
  type LobbyInviteMessage,
  type OnlineStatusMessage
} from '@/websocket/FriendlistClient';
import {LobbyService} from '@/service/LobbyService';
import {LobbyActorRole} from '@/model/dto/LobbyActorRole';

const props = defineProps<{
  lobbyId?: string;
}>();

const friends = ref<FriendEntry[]>([]);
const incomingRequests = ref<FriendRequestEntry[]>([]);
const outgoingRequests = ref<FriendRequestEntry[]>([]);
const lobbyInvites = ref<LobbyInviteMessage[]>([]);
const loading = ref(false);
const sending = ref(false);
const inviting = ref(false);
const joiningInvite = ref(false);
const errorText = ref('');
const infoText = ref('');
const newFriendTarget = ref('');
const wsConnected = ref(false);
const canInvite = computed(() => !!props.lobbyId);

let friendlistClient: FriendlistClient | null = null;
let connectionCheckInterval: number | null = null;

const setError = (message: string) => {
  if (!errorText.value) {
    errorText.value = message;
  }
};

const handleFriendUpdate = (msg: FriendUpdateMessage) => {
  console.log('[Friendlist] Friend update received:', msg);
  loadFriendlist();
};

const handleStatusUpdate = (msg: OnlineStatusMessage) => {
  console.log('[Friendlist] Status update received:', msg);
  const friendIndex = friends.value.findIndex(f => f.username === msg.username);
  if (friendIndex !== -1) {
    friends.value[friendIndex] = {
      ...friends.value[friendIndex] as { id: string; username: string; online: boolean },
      online: msg.online
    };
  }
};

const handleLobbyInvite = (msg: LobbyInviteMessage) => {
  if (!msg?.lobbyId || !msg?.fromUsername) {
    return;
  }

  const exists = lobbyInvites.value.some(invite =>
      invite.lobbyId === msg.lobbyId && invite.fromUsername === msg.fromUsername
  );

  if (!exists) {
    lobbyInvites.value.unshift(msg);
  }
};

const loadFriendlist = async () => {
  loading.value = true;
  errorText.value = '';

  const [friendsResult, requestsResult] = await Promise.all([
    FriendlistService.getFriends(),
    FriendlistService.getRequests()
  ]);

  loading.value = false;

  if (friendsResult.success) {
    friends.value = friendsResult.friends;
  } else {
    friends.value = [];
    setError(friendsResult.error || 'Freundesliste konnte nicht geladen werden');
  }

  if (requestsResult.success) {
    incomingRequests.value = requestsResult.incoming;
    outgoingRequests.value = requestsResult.outgoing;
  } else {
    incomingRequests.value = [];
    outgoingRequests.value = [];
    setError(requestsResult.error || 'Anfragen konnten nicht geladen werden');
  }
};

const reload = async () => {
  await loadFriendlist();
};

const sendRequest = async () => {
  const targetId = newFriendTarget.value.trim();
  infoText.value = '';

  if (!targetId) {
    errorText.value = 'Bitte gib einen Benutzernamen ein';
    return;
  }

  sending.value = true;
  errorText.value = '';

  const result = await FriendlistService.sendRequest(targetId);
  sending.value = false;

  if (result.success) {
    infoText.value = 'Anfrage gesendet';
    newFriendTarget.value = '';
    await loadFriendlist();
  } else {
    errorText.value = result.error || 'Anfrage fehlgeschlagen';
  }
};

const acceptRequest = async (requestId: string) => {
  errorText.value = '';

  const result = await FriendlistService.acceptRequest(requestId);

  if (result.success) {
    infoText.value = 'Anfrage angenommen';
    await loadFriendlist();
  } else {
    errorText.value = result.error || 'Annehmen fehlgeschlagen';
  }
};

const joinInvite = async (invite: LobbyInviteMessage) => {
  errorText.value = '';
  infoText.value = '';
  joiningInvite.value = true;

  const result = await LobbyService.joinLobby(invite.lobbyId, LobbyActorRole.PLAYER);
  joiningInvite.value = false;

  if (result.success) {
    lobbyInvites.value = lobbyInvites.value.filter(item =>
        !(item.lobbyId === invite.lobbyId && item.fromUsername === invite.fromUsername)
    );
    await router.push({name: 'Lobby', params: {lobbyId: invite.lobbyId}});
  } else {
    errorText.value = result.error || 'Einladung konnte nicht angenommen werden';
  }
};

const inviteFriend = async (friend: FriendEntry) => {
  infoText.value = '';
  errorText.value = '';

  if (!props.lobbyId) {
    errorText.value = 'Lobby-ID fehlt fuer die Einladung';
    return;
  }

  inviting.value = true;
  const result = await FriendlistService.inviteToLobby(props.lobbyId, friend.username);
  inviting.value = false;

  if (result.success) {
    infoText.value = `Einladung gesendet an ${friend.username}`;
  } else {
    errorText.value = result.error || 'Einladung fehlgeschlagen';
  }
};

onMounted(() => {
  loadFriendlist();

  friendlistClient = new FriendlistClient(
      handleFriendUpdate,
      handleStatusUpdate,
      handleLobbyInvite
  );

  connectionCheckInterval = window.setInterval(() => {
    wsConnected.value = friendlistClient?.isConnected() ?? false;
  }, 1000);
});

onUnmounted(() => {
  if (friendlistClient) {
    friendlistClient.disconnect();
    friendlistClient = null;
  }

  if (connectionCheckInterval) {
    clearInterval(connectionCheckInterval);
    connectionCheckInterval = null;
  }
});
</script>

<style scoped>
.friendlist-section {
  margin-top: 0;
}

.friend-card {
  background-color: rgba(var(--bs-tertiary-bg-rgb));
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
}

.friendlist-grid {
  display: grid;
  gap: 1rem;
}

.friend-list {
  max-height: 200px;
  overflow-y: auto;
}

.list-group-item {
  background-color: transparent;
  border-color: rgba(255, 255, 255, 0.1);
  color: var(--bs-body-color);
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
  flex-shrink: 0;
}

.status-dot.online {
  background-color: #28a745;
}

.status-dot.offline {
  background-color: #6c757d;
}

.connection-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}

.connection-dot.online {
  background-color: #28a745;
}

.connection-dot.offline {
  background-color: #6c757d;
}

.alert-sm {
  font-size: 0.875rem;
}
</style>
