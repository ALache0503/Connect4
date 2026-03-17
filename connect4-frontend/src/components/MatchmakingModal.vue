<template>
  <div
      class="modal fade show"
      style="display: block;"
      tabindex="-1"
  >
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">Matchmaking gestartet</h5>
        </div>

        <div
            class="modal-body d-flex flex-column justify-content-center align-items-center"
            style="min-height: 150px;"
        >
          <div class="text-center mb-3">
            {{ matchmakingInfo }}
          </div>

          <div v-if="!matchFound">
            <div class="spinner-border text-primary" role="status">
              <span class="visually-hidden">Loading...</span>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button
              :disabled="matchFound"
              class="btn btn-danger"
              @click="leaveMatchmaking"
          >
            Matchmaking verlassen
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {onBeforeUnmount, onMounted, ref} from "vue";
import router from "@/router";
import type {CreateLobbyResponse} from "@/model/dto/CreateLobbyResponse.js";
import type {MatchmakingClient} from "@/websocket/MatchmakingClient.ts";
import {LobbyService} from "@/service/LobbyService.ts";
import {LobbyActorRole} from "@/model/dto/LobbyActorRole.ts";

const matchFound = ref(false);
const matchmakingInfo = ref('');

const emit = defineEmits(['close']);
let mmClient: MatchmakingClient;

const startMatchmaking = async () => {
  matchmakingInfo.value = 'Suche Spieler...';

  const {MatchmakingClient} = await import('@/websocket/MatchmakingClient.ts');
  mmClient = new MatchmakingClient(onMatchFound, onError);
}

const onMatchFound = async (response: CreateLobbyResponse) => {
  matchmakingInfo.value = 'Spieler gefunden!';
  matchFound.value = true;

  const result = await LobbyService.joinLobby(response.lobbyId, LobbyActorRole.PLAYER);

  if (!result.success) {
    onError(result.error ?? "Unbekannter Fehler");
  } else {
    setTimeout(() => {
      router.push({
        name: 'Lobby',
        params: {lobbyId: response.lobbyId}
      });
    }, 1000);
  }
}

const onError = (msg: string) => {
  matchmakingInfo.value = msg;

  setTimeout(() => {
    leaveMatchmaking();
  }, 1000);
}

const leaveMatchmaking = () => {
  emit('close');
}

onMounted(() => {
  startMatchmaking();
});

onBeforeUnmount(() => {
  mmClient.disconnect();
})
</script>
