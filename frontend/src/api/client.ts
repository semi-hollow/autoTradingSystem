import axios from "axios";

const baseURL = import.meta.env.VITE_BACKEND_URL ?? "http://localhost:8080";

export const apiClient = axios.create({
  baseURL,
  timeout: 15000
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error("API error", error);
    return Promise.reject(error);
  }
);
