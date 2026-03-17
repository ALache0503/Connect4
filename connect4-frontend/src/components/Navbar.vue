<template>
  <!doctype html>
  <!-- Example Code Start-->
  <nav class="p-3 m-0 border-0 bd-example m-0 border-0 navbar navbar-expand-lg bg-body-tertiary" data-bs-theme="dark">
    <div class="container-fluid">
      <a class="navbar-brand" href="#">4-Gewinnt MOVS</a>
      <button aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation"
              class="navbar-toggler"
              data-bs-target="#navbarSupportedContent" data-bs-toggle="collapse" type="button">
        <span class="navbar-toggler-icon"></span>
      </button>
      <div id="navbarSupportedContent" class="collapse navbar-collapse">
        <ul class="navbar-nav me-auto mb-2 mb-lg-0">
          <li class="nav-item">
            <RouterLink active-class="active" aria-current="page" class="nav-link" to="/overview">
              Hauptseite
            </RouterLink>
          </li>
          <li class="nav-item">
            <RouterLink active-class="active" aria-current="page" class="nav-link" to="/statistics">
              Statistiken
            </RouterLink>
          </li>
        </ul>
        <p v-if="username !== ''" class="text-light d-flex m-0 me-2">Willkommen, {{ username }}</p>
        <p v-else class="text-light d-flex m-0 me-2">Lädt..</p>
      </div>
    </div>
  </nav>
</template>

<script lang="ts" setup>
import api from "@/api/api.js";
import {onMounted, ref} from "vue";
import {useUserStore} from "@/stores/user.ts";

const username = ref<string>("");

const userStore = useUserStore();

async function loadNavbar() {
  const response = api.get("/users/auth/username");
  username.value = (await response).data;

  userStore.setUsername(username.value);
}

onMounted(async () => {
  await loadNavbar();
})
</script>
