<template>
  <div class="statistics-container" data-bs-theme="dark">
    <div :style="{ height: chatHeight }" class="stats-main-area">
      <div class="container py-4">
        <!-- Header mit Tabs -->
        <div class="d-flex justify-content-between align-items-center mb-4">
          <h2 class="text-light mb-0">Statistiken</h2>
          <div class="btn-group" role="group">
            <button
                :class="activeTab === 'own' ? 'btn-primary' : 'btn-outline-light'"
                class="btn"
                type="button"
                @click="activeTab = 'own'; loadOwnStats()">
              Meine Stats
            </button>
            <button
                :class="activeTab === 'leaderboard' ? 'btn-primary' : 'btn-outline-light'"
                class="btn"
                type="button"
                @click="activeTab = 'leaderboard'; loadLeaderboard()">
              Leaderboard
            </button>
          </div>
        </div>

        <!-- Error/Loading Messages -->
        <div v-if="errorText" class="alert alert-danger" role="alert">
          {{ errorText }}
        </div>
        <div v-if="loading" class="alert alert-info" role="alert">
          Lade Daten...
        </div>

        <!-- Meine Stats & User-Suche Tab -->
        <div v-if="activeTab === 'own'">
          <!-- User-Suche -->
          <div class="mb-4">
            <div class="input-group">
              <input
                  v-model="searchUsername"
                  class="form-control"
                  placeholder="Benutzername eingeben..."
                  type="text"
                  @keyup.enter="searchUser">
              <button class="btn btn-outline-light" @click="searchUser">
                <i class="bi bi-search"></i> Suchen
              </button>
              <button
                  v-if="currentUsername !== userStore.username"
                  class="btn btn-outline-secondary"
                  @click="loadOwnStats">
                <i class="bi bi-arrow-left"></i> Zurück zu meinen Stats
              </button>
            </div>
          </div>

          <!-- Stats Grid -->
          <div v-if="currentStats" class="stats-section">
            <h4 class="text-light mb-3">
              {{ currentUsername === userStore.username ? 'Deine Statistiken' : `Statistiken von ${currentUsername}` }}
            </h4>
            <div class="stats-grid">
              <div class="stat-card">
                <div class="stat-icon">
                  <i class="bi bi-controller"></i>
                </div>
                <div class="stat-content">
                  <div class="stat-label">Gespielte Spiele</div>
                  <div class="stat-value">{{ currentStats.totalGames }}</div>
                </div>
              </div>

              <div class="stat-card">
                <div class="stat-icon text-success">
                  <i class="bi bi-trophy"></i>
                </div>
                <div class="stat-content">
                  <div class="stat-label">Siege</div>
                  <div class="stat-value">{{ currentStats.wins }}</div>
                </div>
              </div>

              <div class="stat-card">
                <div class="stat-icon text-danger">
                  <i class="bi bi-x-circle"></i>
                </div>
                <div class="stat-content">
                  <div class="stat-label">Niederlagen</div>
                  <div class="stat-value">{{ currentStats.losses }}</div>
                </div>
              </div>

              <div class="stat-card">
                <div class="stat-icon text-warning">
                  <i class="bi bi-graph-up"></i>
                </div>
                <div class="stat-content">
                  <div class="stat-label">Siegesrate</div>
                  <div class="stat-value">{{ winRate }}%</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Leaderboard Tab -->
        <div v-if="activeTab === 'leaderboard'">
          <h4 class="text-light mb-3">Top Spieler</h4>
          <div v-if="leaderboard.length > 0" class="table-responsive">
            <table class="table table-dark table-hover">
              <thead>
              <tr>
                <th scope="col">#</th>
                <th scope="col">Benutzername</th>
                <th scope="col">Spiele</th>
                <th scope="col">Siege</th>
                <th scope="col">Niederlagen</th>
                <th scope="col">Siegesrate</th>
              </tr>
              </thead>
              <tbody>
              <tr
                  v-for="(entry, index) in leaderboard"
                  :key="entry.username"
                  :class="{ 'table-primary': entry.username === userStore.username }">
                <th scope="row">{{ index + 1 }}</th>
                <td>
                    <span
                        class="user-link"
                        @click="viewUserStats(entry.username)">
                      {{ entry.username }}
                      <i v-if="entry.username === userStore.username" class="bi bi-star-fill text-warning ms-1"></i>
                    </span>
                </td>
                <td>{{ entry.totalGames }}</td>
                <td class="text-success">{{ entry.wins }}</td>
                <td class="text-danger">{{ entry.losses }}</td>
                <td>{{ calculateWinRate(entry.wins, entry.totalGames) }}%</td>
              </tr>
              </tbody>
            </table>
          </div>
          <div v-else-if="!loading" class="text-muted text-center py-4">
            Keine Daten verfügbar
          </div>
        </div>
      </div>
    </div>

    <!-- Chat Area - resizable -->
    <div class="chat-resize-handle" @mousedown="startResize"></div>
    <div :style="{ height: `calc(100vh - ${chatHeight})` }" class="chat-area-wrapper">
      <ChatArea/>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref} from 'vue';
import {StatisticsService} from '@/service/StatisticsService';
import type {UserStatistics} from '@/model/dto/UserStatistics';
import {useUserStore} from '@/stores/user';
import ChatArea from './ChatArea.vue';

const userStore = useUserStore();

const activeTab = ref<'own' | 'leaderboard'>('own');
const loading = ref(false);
const errorText = ref('');
const searchUsername = ref('');
const currentUsername = ref<string>(userStore.username || '');
const currentStats = ref<UserStatistics | null>(null);
const leaderboard = ref<UserStatistics[]>([]);

// Chat resize functionality
const chatHeight = ref('70vh');
let isResizing = false;

const winRate = computed(() => {
  if (!currentStats.value) return 0;
  return currentStats.value.winRate
      ? Math.round(currentStats.value.winRate)
      : 0;
});

const calculateWinRate = (wins: number, totalGames: number): number => {
  if (totalGames === 0) return 0;
  return Math.round((wins / totalGames) * 100);
};

const loadOwnStats = async () => {
  loading.value = true;
  errorText.value = '';
  currentUsername.value = userStore.username || '';
  searchUsername.value = '';

  const response = await StatisticsService.getUserStatistics(currentUsername.value);
  loading.value = false;

  if (response.success && response.data) {
    currentStats.value = response.data;
  } else {
    errorText.value = response.error || 'Fehler beim Laden der Statistiken';
    currentStats.value = null;
  }
};

const searchUser = async () => {
  if (!searchUsername.value.trim()) {
    errorText.value = 'Bitte einen Benutzernamen eingeben';
    return;
  }

  loading.value = true;
  errorText.value = '';
  currentUsername.value = searchUsername.value.trim();

  const response = await StatisticsService.getUserStatistics(currentUsername.value);
  loading.value = false;

  if (response.success && response.data) {
    currentStats.value = response.data;
  } else {
    errorText.value = response.error || 'Benutzer nicht gefunden';
    currentStats.value = null;
  }
};

const loadLeaderboard = async () => {
  loading.value = true;
  errorText.value = '';

  const response = await StatisticsService.getLeaderboard();
  loading.value = false;

  if (response.success && response.data) {
    leaderboard.value = response.data;
  } else {
    errorText.value = response.error || 'Fehler beim Laden des Leaderboards';
    leaderboard.value = [];
  }
};

const viewUserStats = (username: string) => {
  searchUsername.value = username;
  activeTab.value = 'own';
  currentUsername.value = username;
  searchUser();
};

// Resize functionality
const startResize = (e: MouseEvent) => {
  isResizing = true;
  document.addEventListener('mousemove', handleResize);
  document.addEventListener('mouseup', stopResize);
  e.preventDefault();
};

const handleResize = (e: MouseEvent) => {
  if (!isResizing) return;
  const newHeight = (e.clientY / window.innerHeight) * 100;
  if (newHeight >= 30 && newHeight <= 85) {
    chatHeight.value = `${newHeight}vh`;
  }
};

const stopResize = () => {
  isResizing = false;
  document.removeEventListener('mousemove', handleResize);
  document.removeEventListener('mouseup', stopResize);
};

onMounted(() => {
  loadOwnStats();
});
</script>

<style scoped>
.statistics-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: var(--bs-body-bg);
}

.stats-main-area {
  overflow-y: auto;
  background-color: var(--bs-body-bg);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-top: 1rem;
}

.stat-card {
  background: rgba(var(--bs-tertiary-bg-rgb));
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 1.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.stat-icon {
  font-size: 2.5rem;
  color: var(--bs-primary);
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 0.875rem;
  color: var(--bs-secondary);
  margin-bottom: 0.25rem;
}

.stat-value {
  font-size: 2rem;
  font-weight: bold;
  color: var(--bs-light);
}

.user-link {
  cursor: pointer;
  color: var(--bs-primary);
  text-decoration: underline;
}

.user-link:hover {
  color: var(--bs-info);
}

.table-primary {
  background-color: rgba(13, 110, 253, 0.2) !important;
}

.chat-resize-handle {
  height: 4px;
  background-color: rgba(255, 255, 255, 0.2);
  cursor: ns-resize;
  transition: background-color 0.2s;
}

.chat-resize-handle:hover {
  background-color: var(--bs-primary);
}

.chat-area-wrapper {
  overflow: hidden;
  background-color: var(--bs-dark);
}
</style>