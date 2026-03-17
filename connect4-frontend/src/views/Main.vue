<template>
  <div class="main-layout">
    <Navbar/>
    <div class="content-wrapper">
      <div class="main-content">
        <router-view/>
      </div>
      <aside class="friendlist-sidebar">
        <Friendlist :lobby-id="activeLobbyId"/>
      </aside>
    </div>
    <ChatArea/>
  </div>
</template>

<script lang="ts" setup>
import {computed} from 'vue'
import {useRoute} from 'vue-router'
import Navbar from '@/components/Navbar.vue'
import "bootstrap/dist/css/bootstrap.min.css"
import "bootstrap"
import ChatArea from "@/components/ChatArea.vue";
import Friendlist from "@/components/Friendlist.vue";

const route = useRoute();
const activeLobbyId = computed(() => route.params.lobbyId as string | undefined);
</script>

<style scoped>
.main-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.content-wrapper {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.main-content {
  flex: 1;
  overflow-y: auto;
}

.friendlist-sidebar {
  width: 320px;
  min-width: 280px;
  background-color: var(--bs-body-bg);
  border-left: 1px solid rgba(255, 255, 255, 0.1);
  overflow-y: auto;
  padding: 1rem;
}

</style>