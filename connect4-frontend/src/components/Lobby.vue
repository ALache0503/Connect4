<template>
  <div class="lobby-container py-4" data-bs-theme="dark">
    <div v-if="viewMode === 'LOBBY'" class="container">
      <!-- Lobby Header -->
      <div class="d-flex align-items-center mb-4">
        <h2 class="text-light mb-0 d-flex align-items-center">
          Lobby: {{ lobbyDto.id }}
          <button :class="statusClass" class="btn badge ms-2 d-flex align-items-center justify-content-center"
                  @click="toggleState">{{ lobbyDto.state }}<i class="bi bi-arrow-left-right"></i>
          </button>
        </h2>
        <!-- Copy Button -->
        <button class="btn btn-outline-light btn-sm ms-2 d-flex align-items-center justify-content-center"
                title="Lobby-ID kopieren"
                @click="copyLobbyId">
          <i class="bi bi-clipboard"></i>
        </button>
      </div>

      <div class="row mb-4">
        <div class="alert alert-danger" role="alert">
          {{ errorText }}
        </div>
        <!-- Spielerbereich -->
        <div class="col-md-6">
          <h5 class="text-light">Spieler</h5>
          <div
              v-for="player in lobbyDto.actors.filter(actor => actor.role === LobbyActorRole.PLAYER || actor.role === LobbyActorRole.PLAYER_BOT)"
              class="d-flex align-items-center mb-2 p-2 rounded shadow-sm actor-card">
            <div :style="{ backgroundColor: avatarColor(player.username) }" class="actor-avatar me-3">
              <i v-if="player.role === LobbyActorRole.PLAYER_BOT" class="bi bi-robot">
              </i>
              <div v-else>{{ player.username.charAt(0).toUpperCase() }}</div>
            </div>
            <div class="actor-name">{{ player.username }}</div>
            <button
                v-if="isBotActor(player.username)"
                class="btn btn-outline-danger btn-sm ms-2 d-flex align-items-center justify-content-center"
                title="Bot entfernen"
                @click="removeBot(player.username)"
            >
              <i class="bi bi-person-dash"></i>
            </button>
          </div>
          <div
              v-if="lobbyDto.actors.filter(a => a.role === LobbyActorRole.PLAYER || a.role === LobbyActorRole.PLAYER_BOT).length < 2"
              class="text-muted mt-2">
            Warte auf Spieler...
          </div>
          <!-- Bot-Aktionen -->
          <div class="mt-3 d-flex gap-2">
            <button
                :disabled="!canAddBot()"
                class="btn btn-outline-info btn-sm d-flex align-items-center justify-content-center"
                title="Bot hinzufügen"
                @click="addBot()"
            >
              <i class="bi bi-robot me-1"></i>
              Bot hinzufügen
            </button>
          </div>
        </div>

        <!-- Zuschauerbereich -->
        <div class="col-md-6">
          <h5 class="text-light">Zuschauer</h5>
          <div v-for="spectator in lobbyDto.actors.filter(actor => actor.role === LobbyActorRole.SPECTATOR)"
               class="d-flex align-items-center mb-2 p-2 rounded shadow-sm actor-card">
            <div :style="{ backgroundColor: avatarColor(spectator.username) }" class="actor-avatar me-3">
              {{ spectator.username.charAt(0).toUpperCase() }}
            </div>
            <div class="actor-name">{{ spectator.username }}</div>
          </div>
        </div>
      </div>

      <!-- Buttons -->
      <div class="d-flex gap-2">
        <button v-if="!hasGame" :disabled="!canStartGame()" class="btn btn-primary" @click="startGame">Start
        </button>
        <button v-else class="btn btn-success" @click="goToGame">Zum Spiel</button>
        <button class="btn btn-outline-light" @click="leaveLobby">Verlassen</button>
        <button :disabled="!canRoleToggle" class="btn btn-secondary" @click="toggleRole">Rolle wechseln</button>
      </div>
    </div>
    <div v-else class="container">
      <!-- Header -->
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h2 class="text-light mb-0">Spiel: {{ lobbyId }}</h2>
        <div class="d-flex gap-2">
          <span class="badge bg-primary">Dran: {{ currentPlayer }}</span>
          <span v-if="isGameOver" class="badge bg-warning">{{ gameOverMessage }}</span>
          <span v-else-if="isMyTurn" class="badge bg-success">Du bist dran</span>
          <button class="btn btn-outline-light btn-sm" @click="leaveLobby">Verlassen</button>
        </div>
      </div>


      <!-- Status -->
      <div v-if="errorText" class="alert alert-danger">{{ errorText }}</div>
      <div v-if="!gameLoaded" class="alert alert-warning">
        <span v-if="loading" class="spinner-border spinner-border-sm me-2" role="status"></span>
        {{ loading ? 'Verbinde mit Server...' : 'Spiel nicht gefunden (HTTP 400)' }}
      </div>

      <!-- Spielbrett 6x7 -->
      <div v-if="!loading && boardState.length > 0" class="game-board">
        <div class="column-indicators mb-3">
          <div v-for="n in 7" :key="n" class="col-indicator">{{ n }}</div>
        </div>

        <div v-for="(row, rowIndex) in boardState" :key="rowIndex" class="board-row">
          <div
              v-for="(cell, colIndex) in row"
              :key="colIndex"
              :class="[
                  'board-cell', getCellClass(cell), {
                'disabled': !isMyTurn || isColumnFull(colIndex)
                  }
                ]"
              @click="dropPiece(colIndex)"
          >
            <div class="cell-content"></div>
          </div>
        </div>
      </div>

      <!-- Game Over -->
      <div v-if="gameLoaded && isGameOver" class="mt-4 text-center">
        <h3 class="text-light mb-3">
          {{ gameOverMessage }}
        </h3>
      </div>

    </div>
  </div>
</template>
<script lang="ts" setup>

import {computed, onBeforeUnmount, onMounted, ref} from 'vue';
import {LobbyService} from "@/service/LobbyService.ts";
import {LobbyDTO} from "@/model/dto/LobbyDTO.ts";
import {LobbyState} from "@/model/enum/LobbyState.ts";
import {LobbyUpdateType} from "@/model/enum/LobbyUpdateType.ts";
import router from "@/router";
import {Routes} from "@/model/enum/Routes.ts";
import {LobbyActorRole} from "@/model/dto/LobbyActorRole.ts";
import type {LobbyActor} from "@/model/dto/LobbyActor.ts";
import {useUserStore} from "@/stores/user.ts";
import api from "@/api/api.ts";
import type {GameDTO} from '@/model/dto/GameDTO';
import {GameBoardCellState} from "@/model/enum/GameBoarCellState.ts";
import {LobbyClient} from "@/websocket/LobbyClient";
import {eventBus} from "@/bus/eventBus.ts";
import {ChatType} from "@/model/enum/ChatType.ts";

const props = defineProps<{
  lobbyId: string
}>();

const viewMode = ref<'LOBBY' | 'GAME'>('LOBBY')

const gameDto = ref<GameDTO | null>(null);

const hasGame = computed(() => !!gameDto.value);

const loading = ref(false);

const username = computed(() => userStore.username);

const currentPlayer = computed(() => gameDto.value?.currentTurn ?? '');

const isMyTurn = computed(() =>
    gameDto.value?.currentTurn === username.value
    && gameDto.value.turnResult !== 'DRAW'
    && gameDto.value.state !== 'ENDED'
);

const gameLoaded = computed(() => !!gameDto.value);

const gameStatus = computed(() => gameDto.value?.state ?? 'NONE');

const winnerUsername = computed(() => {
  if (!gameDto.value || gameDto.value.state !== 'ENDED') return null;
  return gameDto.value.currentTurn;
});

const boardState = computed(() => {
  if (!gameDto.value) return [];

  return gameDto.value.board.grid.map(row =>
      row.map(cell => cell === 'EMPTY' ? null : cell)
  );
});

const loadGame = async () => {
  loading.value = true;
  try {
    const res = await api.get(`/game/${props.lobbyId}`);
    gameDto.value = res.data;
  } catch (e: any) {
    errorText.value = e.response?.data || 'Spiel nicht gefunden';
  } finally {
    loading.value = false;
  }
};

const dropPiece = async (column: number) => {
  if (!isMyTurn.value || isColumnFull(column)) return;

  try {
    await api.post(`/game/turn/${props.lobbyId}`, {
      column
    });
  } catch (e: any) {
    errorText.value = e.response?.data || 'Ungültiger Zug';
  }
};

const isColumnFull = (col: number): boolean => {
  const game = gameDto.value;
  if (!game) return true;

  const firstRow = game.board.grid[0];
  if (!firstRow) return true;

  return firstRow[col] !== GameBoardCellState.EMPTY;
};


const startGame = async () => {
  try {
    await api.post(`/game/create/${lobbyDto.value.id}`);
    errorText.value = '';
    viewMode.value = 'GAME';
    await loadGame();
  } catch (e: any) {
    errorText.value = e.response?.data || 'Spiel konnte nicht gestartet werden';
  }
};

const getCellClass = (cell: GameBoardCellState | null) => {
  switch (cell) {
    case GameBoardCellState.PLAYER_1:
      return "player-red";
    case GameBoardCellState.PLAYER_2:
      return "player-yellow";
    default:
      return "";
  }
};

const errorText = ref('');
const lobbyDto = ref<LobbyDTO>(new LobbyDTO('LOADING', [], LobbyState.CLOSED));
const userStore = useUserStore();

let lobbyWs: LobbyClient;

const isGameOver = computed(() => gameDto.value?.state === 'ENDED');


const gameOverMessage = computed(() => {
  if (!gameDto.value) return '';

  if (gameDto.value.turnResult === 'DRAW') {
    return 'Unentschieden!';
  }

  if (gameDto.value.turnResult === 'WON') {
    return `${gameDto.value.username} hat gewonnen!`
  }

  return '';
});
const loadLobby = async () => {
  const response = await LobbyService.getLobby(props.lobbyId);

  if (response.success && response.lobbyDto) {
    lobbyDto.value = response.lobbyDto;
    errorText.value = 'Lobby geladen!';

    const {LobbyClient} = await import('@/websocket/LobbyClient');
    lobbyWs = new LobbyClient(props.lobbyId, handleLobbyUpdate, handleGameEvent);

    // CHAT
    eventBus.emit("chat:openChannel", {chatType: ChatType.LOBBY, targetId: lobbyDto.value.id})
  } else {
    await router.push(Routes.OVERVIEW); //ToDO add error
  }
}

const handleLobbyUpdate = async (event: any) => {
  lobbyDto.value = event.lobby;
  if (event.type === LobbyUpdateType.GAME_START) {
    errorText.value = '';
    viewMode.value = 'GAME';
    await loadGame();
  }

  // Only needed for Chat MSG
  switch (event.type) {
    case LobbyUpdateType.JOIN: {
      break;
    }
    case LobbyUpdateType.LEFT:
    case LobbyUpdateType.TIMEOUT: {
      break;
    }
    case LobbyUpdateType.STATE_CHANGE: {
      break;
    }
    case LobbyUpdateType.ROLE_CHANGE: {
      break;
    }
  }
}

const leaveLobby = async () => {
  await router.push(Routes.OVERVIEW);
}

const goToGame = async () => {
  viewMode.value = 'GAME';
}

const canStartGame = () => {
  const playingActors = lobbyDto.value.actors.filter(a => a.role === LobbyActorRole.PLAYER || a.role === LobbyActorRole.PLAYER_BOT);

  return (playingActors.length >= 2);
}

const canAddBot = () => {
  const playingActors = lobbyDto.value.actors.filter(a => a.role === LobbyActorRole.PLAYER || a.role === LobbyActorRole.PLAYER_BOT);

  return (playingActors.length < 2);
}

const addBot = () => {
  errorText.value = 'Bot hinzugefügt!';

  api.post(`lobby/addbot/${lobbyDto.value.id}`);
}

const isBotActor = (botName: string) => {
  const actor = getActor(botName);

  return actor.role === LobbyActorRole.PLAYER_BOT;
}

const removeBot = (botName: string) => {
  errorText.value = `Bot '${botName}' entfernt!`;

  api.post(`lobby/removebot/${lobbyDto.value.id}`, {
    botName: botName
  });
}


const toggleState = async () => {
  const actor = getActor(userStore.username);
  const newState = lobbyDto.value.state === LobbyState.CLOSED ? LobbyState.OPEN : LobbyState.CLOSED;
  const playingActors = lobbyDto.value.actors.filter(a => a.role === LobbyActorRole.PLAYER);

  if (actor.role === LobbyActorRole.SPECTATOR) {
    errorText.value = 'Du kannst den Lobby Status nicht setzen!';
  } else {
    if (newState === LobbyState.OPEN && playingActors.length >= 2) {
      errorText.value = 'Bei genügend Spieler, kann die Lobby nicht geöffnet werden!';
    } else {
      const response = await LobbyService.setState(lobbyDto.value.id, newState);

      if (response.success) {
        errorText.value = 'Lobby ' + newState;
      } else {
        errorText.value = response.error || 'Unbekannter Fehler';
      }
    }
  }
}

const canRoleToggle = () => {
  const actor = getActor(userStore.username);
  const newRole = actor.role === LobbyActorRole.PLAYER ? LobbyActorRole.SPECTATOR : LobbyActorRole.PLAYER;
  const playingActors = lobbyDto.value.actors.filter(a => a.role === LobbyActorRole.PLAYER);

  return (newRole !== LobbyActorRole.PLAYER || playingActors.length < 2);
}

const toggleRole = async () => {
  if (canRoleToggle()) {
    const actor = getActor(userStore.username);
    const newRole = actor.role === LobbyActorRole.PLAYER ? LobbyActorRole.SPECTATOR : LobbyActorRole.PLAYER;

    const response = await LobbyService.setRole(lobbyDto.value.id, newRole);

    if (response.success) {
      errorText.value = 'Neue Rolle ' + newRole;
    } else {
      errorText.value = response.error || 'Unbekannter Fehler';
    }
  } else {
    errorText.value = 'Kein freier Spieler Platz!';
  }
}

const getActor = (username: string): LobbyActor => {
  return <LobbyActor>lobbyDto.value.actors.find(actor => actor.username === username);
}

const copyLobbyId = () => {
  navigator.clipboard.writeText(lobbyDto.value.id);
};

const statusClass = computed(() => {
  return lobbyDto.value.state === LobbyState.OPEN ? 'bg-success' : 'bg-danger';
});

const avatarColor = (username: string) => {
  let hash = 0;

  for (let i = 0; i < username.length; i++) {
    hash = username.charCodeAt(i) + ((hash << 5) - hash);
  }

  return `hsl(${hash % 360}, 60%, 50%)`;
};

const handleGameEvent = (event: any) => {
  gameDto.value = {
    ...event.game,
    turnResult: event.turnResult,
    username: event.username
  }
};

onMounted(async () => {
  await loadLobby();

  lobbyWs = new LobbyClient(
      props.lobbyId,
      handleLobbyUpdate,
      handleGameEvent
  );
});


onBeforeUnmount(async () => {
  eventBus.emit("chat:closeChannel", {chatType: ChatType.LOBBY, targetId: lobbyDto.value.id})

  await lobbyWs.disconnect();
});
</script>
<style scoped>
.lobby-container {
  width: 100%;
  background-color: var(--bs-body-bg);
  padding-top: 2rem;
  padding-bottom: 2rem;
}

.actor-card {
  background-color: rgba(var(--bs-tertiary-bg-rgb));
  color: var(--bs-body-color);
}

.actor-avatar {
  width: 40px;
  height: 40px;
  background-color: var(--bs-primary);
  color: white;
  font-weight: bold;
  font-size: 1.2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.actor-name {
  font-weight: 500;
}

/* Badges */
.badge {
  font-size: 0.85rem;
}

.game-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
}

.game-board {
  max-width: 650px;
  margin: 0 auto;
  background: #0d1117;
  border-radius: 25px;
  padding: 30px;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.6);
}

.column-indicators {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 15px;
}

.col-indicator {
  width: 80px;
  height: 30px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  color: #fff;
  font-size: 14px;
}

.board-row {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 10px;
}

.board-cell {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #333;
  border: 8px solid #1f2937;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.board-cell.disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.board-cell.player-red .cell-content {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #ff4444;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 4px 12px rgba(255, 68, 68, 0.5);
}

.board-cell.player-yellow .cell-content {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #ffd700;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 4px 12px rgba(255, 215, 0, 0.5);
}
</style>