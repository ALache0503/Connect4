import {createPinia} from "pinia";
import './assets/main.css'
import {createApp} from 'vue'
import App from "@/App.vue";
import router from './router'
import 'bootstrap-icons/font/bootstrap-icons.css';

(window as any).global = window;

const app = createApp(App);
const pinia = createPinia();

app.use(router);
app.use(pinia);

app.mount('#app');

