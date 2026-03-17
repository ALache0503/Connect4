import axios, {type AxiosError, type AxiosInstance, type AxiosRequestConfig, HttpStatusCode} from 'axios'
import router from "@/router";
import {Routes} from "@/model/enum/Routes.ts";

const API_BASE_URL = "/api/v1";

let isRefreshing = false;

const api: AxiosInstance = axios.create({
    baseURL: API_BASE_URL,
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json'
    }
});

// Retry on 403
api.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
        const status = error.response?.status;
        const originalRequest = error.config as AxiosRequestConfig & {
            _retry?: boolean;
        };

        if (status === HttpStatusCode.Unauthorized && !originalRequest._retry) {
            originalRequest._retry = true;

            try {
                // Prevent refresh loop
                if (!isRefreshing) {
                    isRefreshing = true;

                    await axios.post(
                        `${API_BASE_URL}/auth/refresh`,
                        {},
                        {withCredentials: true}
                    );

                    isRefreshing = false;

                    return api(originalRequest);
                } else {
                    throw new Error("Refresh in progress, skip retry");
                }
            } catch (refreshError) {
                isRefreshing = false;
                await router.push(Routes.LOGIN);

                return Promise.reject(refreshError);
            }
        }

        // second 401
        if (status === HttpStatusCode.Unauthorized) {
            await router.push(Routes.LOGIN);
        }

        return Promise.reject(error);
    }
);

export default api;
