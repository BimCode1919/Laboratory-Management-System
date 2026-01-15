// src/api/AxiosInstances.ts

import axios, { type AxiosRequestConfig } from "axios";
import { API_BASE_URL, REFRESH_TOKEN_ENDPOINT, LOGOUT_ENDPOINT } from "./Endpoints";
import type { BaseResponse } from "../types/BaseResponse";
import { refreshAccessToken } from "./AuthRefresh";
import { navigateTo } from "@/utils/Navigation";
import type { AxiosError } from "axios";

const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
});

let isRefreshing = false;
let failedQueue: {
  resolve: (value: unknown) => void;
  reject: (reason?: unknown) => void;
  config: AxiosRequestConfig;
}[] = [];

const processQueue = (
  error: AxiosError | null,
  token: string | null = null
) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else if (token) {
      prom.config.headers = {
        ...prom.config.headers,
        Authorization: `Bearer ${token}`,
      };
      prom.resolve(api(prom.config));
    }
  });
  failedQueue = [];
};

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token && config.url !== REFRESH_TOKEN_ENDPOINT && config.url !== LOGOUT_ENDPOINT) {
    if (!config.headers) {
      config.headers = {};
    }
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(async (response) => {
  const data = response.data as BaseResponse<unknown>;
  const originalRequest = response.config;
  const isUnauthorized = response.status === 401;

  const isInternalAuthError =
    data &&
    typeof data.code === "string" &&
    ((data.message &&
      data.message.toLowerCase().includes("invalid or expired jwt token")) ||
      data.code.startsWith("401"));

  const isNotRefreshRequest = originalRequest.url !== REFRESH_TOKEN_ENDPOINT;

  if ((isUnauthorized || isInternalAuthError) && isNotRefreshRequest) {
    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        failedQueue.push({ resolve, reject, config: originalRequest });
      });
    }

    isRefreshing = true;
    console.log("Activating refreshing token (from response interceptor)...");

    try {
      const newAccessToken = await refreshAccessToken();

      isRefreshing = false;

      if (newAccessToken) {
        processQueue(null, newAccessToken);

        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        return api(originalRequest);
      } else {
        throw new Error("Token refresh failed. New access token is null.");
      }
    } catch (e) {
      isRefreshing = false;
      localStorage.clear();
      const rejectError = new axios.AxiosError(
        'Token Refresh Failed',
        'TOKEN_REFRESH_ERROR',
        originalRequest,
        null,
        response
      );
      processQueue(rejectError);
      console.error("Error: Invalid or expired refresh token. Redirecting.", e);
      navigateTo("/");
      return Promise.reject(rejectError);
    }
  }

  return response;
});

api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

export { api };
export default api;