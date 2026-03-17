<template>
  <div class="login-container">
    <h2>Login</h2>
    <form @submit.prevent="handleLogin">
      <div>
        <label for="username">Username:</label>
        <input id="username" v-model="username" required type="text"/>
      </div>
      <div>
        <label for="password">Password:</label>
        <input id="password" v-model="password" required type="password"/>
      </div>
      <div class="action">
        <button type="submit">Login</button>
        <button type="button" @click="handleRegister">Registrieren</button>
      </div>
    </form>
  </div>
</template>

<script setup>
import {ref} from 'vue'
import {useRouter} from 'vue-router'
import axios from 'axios'

const router = useRouter()

const username = ref('')
const password = ref('')

// TODO use api in this component!
const handleLogin = async () => {
  try {
    await axios.post("http://localhost:8080/api/v1/auth/login", {
      username: username.value,
      password: password.value
    }, {withCredentials: true});

    await router.push('/overview');
  } catch (e) {
    console.log('error: ' + e);
  }
}

const handleRegister = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/v1/auth/register', { //URL anpassen
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({
        username: username.value,
        password: password.value
      })
    })

    if (!response.ok) throw new Error('Register fehlgeschlagen')

    console.log('Registrierung erfolgreich')
  } catch (error) {
    // gibt den fehler in der console aus
    console.error(error)
  }
}
</script>

<style scoped>
.login-container {
  max-width: 300px;
  margin: 50px auto;
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 5px;
}

.login-container input {
  width: 100%;
  margin-bottom: 10px;
  padding: 5px;
}
</style>
