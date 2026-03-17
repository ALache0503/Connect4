import {createRouter, createWebHistory} from 'vue-router'
import Login from "@/views/Login.vue";
import Main from "@/views/Main.vue";
import MainContent from "@/components/MainContent.vue";
import Lobby from "@/components/Lobby.vue";
import Statistics from "@/components/Statistics.vue";
import {Routes} from "@/model/enum/Routes.ts";

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: Routes.LOGIN,
            component: Login
        },
        {
            path: '/',
            component: Main,
            redirect: () => {
                return Routes.OVERVIEW;
            },
            children: [
                {
                    path: Routes.OVERVIEW,
                    name: 'MainContent',
                    component: MainContent
                },
                {
                    path: Routes.LOBBY + '/:lobbyId',
                    name: 'Lobby',
                    component: Lobby,
                    props: true
                },
                {
                    path: Routes.STATISTICS,
                    name: 'Statistic',
                    component: Statistics
                }
            ]
        }
    ]
});

export default router;