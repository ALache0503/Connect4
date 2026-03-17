<template>
  <div class="container py-4">
    <MatchmakingModal v-if="showMatchmakingModal" @close="leaveMatchmaking"/>

    <div class="mb-5 text-center">
      <h1 class="fw-bold">Willkommen zu 4 Gewinnt!</h1>
      <h4 class="text-muted">Viel Spaß beim Spielen!</h4>
    </div>

    <div class="mb-4 text-center">
      <button class="btn btn-primary" @click="startMatchmaking">
        Matchmaking starten
      </button>
    </div>

    <div class="mb-4 text-center">
      <button class="btn btn-success" @click="createLobby">
        Lobby erstellen
      </button>
    </div>

    <div class="mb-4 d-flex flex-column align-items-center">
      <div class="d-flex mb-2" style="max-width: 400px; width: 100%;">
        <input
            v-model="lobbyName"
            class="form-control me-2"
            placeholder="Lobby Name"
            type="text"
        />
        <button class="btn btn-warning" @click="joinLobby">
          Lobby beitreten
        </button>
      </div>
      <div class="text-danger">
        {{ joinError }}
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent, ref} from "vue";
import {LobbyService} from "@/service/LobbyService";
import router from "@/router";
import MatchmakingModal from "@/components/MatchmakingModal.vue";
import {LobbyActorRole} from "@/model/dto/LobbyActorRole.ts";

export default defineComponent({
  name: "MainContent",
  components: {MatchmakingModal},

  setup() {
    const lobbyId = ref<string>("");
    const joinError = ref<string>("");
    const showMatchmakingModal = ref(false);

    const startMatchmaking = () => {
      showMatchmakingModal.value = true;
    };

    const leaveMatchmaking = () => {
      showMatchmakingModal.value = false;
    }

    const createLobby = async () => {
      const result = await LobbyService.createLobby();

      if (!result.success) {
        joinError.value = result.error ?? "Unbekannter Fehler";
      } else {
        joinError.value = "";
        lobbyId.value = result.lobbyId ?? '';
        await joinLobby();
      }
    };

    const joinLobby = async () => {
      const result = await LobbyService.joinLobby(lobbyId.value, LobbyActorRole.PLAYER);

      if (!result.success) {
        joinError.value = result.error ?? "Unbekannter Fehler";
      } else {
        await router.push({
          name: 'Lobby',
          params: {lobbyId: lobbyId.value}
        });
      }
    };

    return {
      lobbyName: lobbyId,
      joinError,
      showMatchmakingModal,
      startMatchmaking,
      leaveMatchmaking,
      createLobby,
      joinLobby,

    };
  }
});

</script>
<style scoped>
</style>
