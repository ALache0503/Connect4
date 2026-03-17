<template>
  <div class="chat-root" data-bs-theme="dark">
    <!-- OPEN PANEL -->
    <div v-if="isOpen" :style="{ bottom: panelBottomPx }" class="card shadow chat-panel">
      <div class="card-header d-flex align-items-center justify-content-between">
        <div class="fw-semibold text-truncate">
          {{ activeChannel?.name ?? "Chat" }}
        </div>
        <button class="btn btn-sm btn-outline-light" type="button" @click="isOpen = false">✕</button>
      </div>

      <div class="card-body p-0 d-flex flex-column" style="height: 360px">
        <div ref="scrollEl" class="p-3 flex-grow-1 overflow-auto">
          <div v-if="activeMessages.length === 0" class="text-secondary small">
            Noch keine Nachrichten in diesem Channel.
          </div>

          <div v-for="(m, idx) in activeMessages" :key="messageKey(m, idx)" class="mb-2">
            <div :class="m.author === userStore.username ? 'justify-content-end' : ''"
                 class="d-flex gap-2 align-items-start">
              <div
                  :style="{ backgroundColor: avatarColor(m.author) }"
                  :title="m.author"
                  aria-hidden="true"
                  class="avatar rounded-circle d-inline-flex align-items-center justify-content-center flex-shrink-0"
              >
                {{ avatarInitial(m.author) }}
              </div>

              <div style="max-width: 78%">
                <div class="small text-secondary">
                  {{ formatTime(m.createdAt) }} · {{ m.author }}
                </div>

                <div
                    :class="m.author === 'me' ? 'bg-primary text-white' : 'bg-body-tertiary'"
                    class="px-2 py-1 rounded-2"
                >
                  {{ m.text }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <form class="border-top p-2 d-flex gap-2" @submit.prevent="send">
          <input v-model="draft" autocomplete="off" class="form-control" placeholder="Nachricht…"/>
          <button :disabled="!draft.trim()" class="btn btn-success">Senden</button>
        </form>
      </div>
    </div>

    <!-- DOCK -->
    <div ref="dockEl" class="fixed-bottom chat-dock border-top">
      <button
          :aria-label="isOpen ? 'Chat schließen' : 'Chat öffnen'"
          :title="isOpen ? 'Schließen' : 'Öffnen'"
          class="btn btn-sm btn-dark shadow-lg position-absolute top-0 start-50 translate-middle dock-arrow-btn"
          type="button"
          @click="toggleOpen"
      >
        <span v-if="!isOpen">▲</span>
        <span v-else>▼</span>
      </button>

      <div class="container-fluid py-2">
        <div class="d-flex flex-column gap-2">
          <div class="d-flex flex-wrap gap-2 align-items-center channels-wrap">
            <button
                v-for="ch in channels"
                :key="ch.id"
                :class="ch.id === activeChannelId ? 'btn-primary' : 'btn-outline-secondary'"
                :title="ch.name"
                class="btn btn-sm position-relative channel-btn"
                type="button"
                @click="selectChannel(ch.id)"
            >
              <span class="d-inline-block text-truncate channel-text">{{ ch.name }}</span>

              <span
                  v-if="(unreadByChannel[ch.id] ?? 0) > 0"
                  class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger"
              >
                {{ unreadByChannel[ch.id] }}
              </span>
            </button>
          </div>

          <div class="d-flex align-items-center gap-2">
            <button
                :disabled="isOpen"
                :title="collapsedPreviewTitle"
                class="btn btn-sm btn-outline-secondary text-start flex-grow-1 preview-btn"
                type="button"
                @click="openPanel"
            >
              <span class="text-truncate ms-1">{{ collapsedPreviewText }}</span>
            </button>

            <button
                :title="isOpen ? 'Schließen' : 'Öffnen'"
                class="btn btn-success btn-sm position-relative flex-shrink-0"
                type="button"
                @click="toggleOpen"
            >
              {{ isOpen ? "Schließen" : "Öffnen" }}
            </button>

            <button
                class="btn btn-sm btn-outline-secondary flex-shrink-0"
                title="Alle Nachrichten löschen"
                type="button"
                @click="clearAll"
            >
              Leeren
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch} from "vue";
import {ChatClient} from "@/websocket/ChatClient";
import {ChatType} from "@/model/enum/ChatType";
import {ChatMessageOutboundDTO} from "@/model/dto/ChatMessageOutboundDTO";
import {useUserStore} from "@/stores/user.ts";
import {eventBus} from "@/bus/eventBus.ts";

type Channel = {
  id: string;       // UI id: "global" | "lobby:123" | "dm:42"
  name: string;
  type: ChatType;
  targetId: string; // server target: "global" | "123" | "42"
};

const userStore = useUserStore();

const channels = ref<Channel[]>([
  {id: "global", name: "Global", type: ChatType.GLOBAL, targetId: "global"}
]);

const activeChannelId = ref<string>("global");
const isOpen = ref<boolean>(false);

const messages = reactive<Record<string, ChatMessageOutboundDTO[]>>({
  global: []
});

const unreadByChannel = reactive<Record<string, number>>({
  global: 0
});

const draft = ref<string>("");

const scrollEl = ref<HTMLElement | null>(null);
const dockEl = ref<HTMLElement | null>(null);
const dockHeight = ref<number>(72);
const panelBottomPx = computed<string>(() => `${dockHeight.value + 12}px`);

const activeChannel = computed<Channel | null>(() => {
  return channels.value.find(c => c.id === activeChannelId.value) ?? null;
});

const activeMessages = computed<ChatMessageOutboundDTO[]>(() => {
  return messages[activeChannelId.value] ?? [];
});

const lastMessage = computed<ChatMessageOutboundDTO | null>(() => {
  return activeMessages.value.slice(-1)[0] ?? null;
});

const collapsedPreviewText = computed<string>(() => {
  if (!lastMessage.value) return "Keine Nachrichten.";
  return `${lastMessage.value.author}: ${lastMessage.value.text}`;
});

const collapsedPreviewTitle = computed<string>(() => {
  if (!lastMessage.value) return "Keine Nachrichten";
  return `${lastMessage.value.author} · ${new Date(lastMessage.value.createdAt).toLocaleString()}`;
});

const chatClient = new ChatClient(receiveMessage);

const subscribed = new Set<string>(); // key = `${type}:${targetId}`

function ensureSubscribed(ch: Channel): void {
  if (ch.type === ChatType.GLOBAL) return;
  const key = `${ch.type}:${ch.targetId}`;
  if (subscribed.has(key)) return;

  chatClient.subscribe(ch.targetId, ch.type);
  subscribed.add(key);
}

function openPanel(): void {
  isOpen.value = true;
  markRead(activeChannelId.value);
}

function toggleOpen(): void {
  isOpen.value = !isOpen.value;
  if (isOpen.value) markRead(activeChannelId.value);
}

function selectChannel(id: string): void {
  activeChannelId.value = id;

  const ch = channels.value.find(c => c.id === id);
  if (ch) ensureSubscribed(ch);

  if (isOpen.value) markRead(id);
}

function markRead(channelId: string): void {
  unreadByChannel[channelId] = 0;
  nextTick(scrollToBottom);
}

function scrollToBottom(): void {
  const el = scrollEl.value;
  if (!el) return;
  el.scrollTop = el.scrollHeight;
}

function send(): void {
  const text = draft.value.trim();
  if (!text) return;

  const ch = activeChannel.value;
  if (!ch) return;

  ensureSubscribed(ch);

  chatClient.send(ch.targetId, text, ch.type);

  draft.value = "";
  nextTick(scrollToBottom);
}

function receiveMessage(msg: ChatMessageOutboundDTO): void {
  const channelId = ensureChannelForMessage(msg);

  (messages[channelId] ||= []).push(msg);

  const isActiveVisible = isOpen.value && channelId === activeChannelId.value;
  if (isActiveVisible) {
    nextTick(scrollToBottom);
  } else {
    unreadByChannel[channelId] = (unreadByChannel[channelId] ?? 0) + 1;
  }
}

function ensureChannelForMessage(msg: ChatMessageOutboundDTO): string {
  // GLOBAL
  if (msg.type === ChatType.GLOBAL || msg.targetId === "global") {
    return "global";
  }

  const existing = channels.value.find(c => c.type === msg.type && c.targetId === msg.targetId);
  if (existing) return existing.id;

  const idPrefix = msg.type === ChatType.DM ? "dm" : "lobby";
  const id = `${idPrefix}:${msg.targetId}`;

  const name =
      msg.type === ChatType.DM ? `DM ${msg.targetId}` :
          msg.type === ChatType.LOBBY ? `Lobby ${msg.targetId}` :
              `Chat ${msg.targetId}`;

  const ch: Channel = {id, name, type: msg.type, targetId: msg.targetId};
  channels.value.push(ch);

  messages[id] ||= [];
  unreadByChannel[id] = unreadByChannel[id] ?? 0;

  ensureSubscribed(ch);
  return id;
}

function clearAll(): void {
  for (const ch of channels.value) {
    messages[ch.id] = [];
    unreadByChannel[ch.id] = 0;
  }
}

function formatTime(ts: number): string {
  try {
    return new Date(ts).toLocaleTimeString([], {hour: "2-digit", minute: "2-digit"});
  } catch {
    return "";
  }
}

function avatarInitial(username: string): string {
  const s = String(username || "?").trim();
  return (s[0] || "?").toUpperCase();
}

function avatarColor(username: string): string {
  const s = String(username || "user");
  let h = 0;
  for (let i = 0; i < s.length; i++) h = (h * 31 + s.charCodeAt(i)) >>> 0;
  return `hsl(${h % 360} 70% 40%)`;
}

function messageKey(m: ChatMessageOutboundDTO, idx: number): string {
  return `${m.type}|${m.targetId}|${m.author}|${m.createdAt}|${idx}`;
}

// ---- Dock height measurement
let ro: ResizeObserver | null = null;

function updateDockHeight(): void {
  const el = dockEl.value;
  if (!el) return;
  dockHeight.value = Math.round(el.getBoundingClientRect().height);
}

function createChannel(chatType: ChatType, targetId: string): void {
  // GLOBAL ist bei dir fest "global" und wird vom ChatClient sowieso subscribed.
  if (chatType === ChatType.GLOBAL) {
    activeChannelId.value = "global";
    if (isOpen.value) markRead("global");
    return;
  }

  const prefix = chatType === ChatType.DM ? "dm" : "lobby";
  const id = `${prefix}:${targetId}`;

  const existing = channels.value.find(c => c.id === id);
  if (existing) {
    activeChannelId.value = existing.id;
    ensureSubscribed(existing);
    if (isOpen.value) markRead(existing.id);
    return;
  }

  const name =
      chatType === ChatType.DM ? `DM ${targetId}` :
          chatType === ChatType.LOBBY ? `Lobby ${targetId}` :
              `Chat ${targetId}`;

  const ch: Channel = {id, name, type: chatType, targetId};
  channels.value.push(ch);

  messages[id] ||= [];
  unreadByChannel[id] = unreadByChannel[id] ?? 0;

  ensureSubscribed(ch);
  activeChannelId.value = id;

  if (isOpen.value) markRead(id);
}

function closeChannel(chatType: ChatType, targetId: string): void {
  if (chatType === ChatType.GLOBAL) return;

  const prefix = chatType === ChatType.DM ? "dm" : "lobby";
  const id = `${prefix}:${targetId}`;

  const idx = channels.value.findIndex(c => c.id === id);
  if (idx !== -1) channels.value.splice(idx, 1);

  delete messages[id];
  delete unreadByChannel[id];

  if (activeChannelId.value === id) {
    activeChannelId.value = "global";
    if (isOpen.value) markRead("global");
  }

  chatClient.unsubscribe(targetId, chatType);
}

const onOpenChannel = (p: { chatType: ChatType; targetId: string }) => {
  createChannel(p.chatType, p.targetId);
};

const onCloseChannel = (p: { chatType: ChatType; targetId: string }) => {
  closeChannel(p.chatType, p.targetId);
};

onMounted(() => {
  updateDockHeight();
  ro = new ResizeObserver(updateDockHeight);
  if (dockEl.value) ro.observe(dockEl.value);

  window.addEventListener("resize", updateDockHeight);
  nextTick(scrollToBottom);

  const ch = activeChannel.value;
  if (ch) ensureSubscribed(ch);

  eventBus.on("chat:openChannel", onOpenChannel);
  eventBus.on("chat:closeChannel", onCloseChannel);
});

onBeforeUnmount(async () => {
  if (ro) ro.disconnect();
  window.removeEventListener("resize", updateDockHeight);
  await chatClient.disconnect();

  eventBus.off("chat:openChannel", onOpenChannel);
});

watch(isOpen, (v) => {
  if (v) nextTick(scrollToBottom);
});
</script>

<style scoped>
.chat-panel {
  position: fixed;
  left: 0.75rem;
  right: 0.75rem;
  z-index: 1080;
}

.chat-dock {
  background: var(--bs-body-bg);
  z-index: 1090;
}

.chat-dock, .chat-dock .container-fluid, .channels-wrap, .channel-btn {
  overflow: visible;
}

.channels-wrap {
  max-width: 100%;
}

.channel-text {
  max-width: 10rem;
  vertical-align: bottom;
}

.avatar {
  width: 32px;
  height: 32px;
  color: #fff;
  font-weight: 700;
  font-size: 0.9rem;
  line-height: 1;
}

.preview-btn {
  min-width: 180px;
}
</style>
