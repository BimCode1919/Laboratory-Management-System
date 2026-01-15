// src/api/authRefresh.ts

import { REFRESH_TOKEN_ENDPOINT } from "./Endpoints";
import api from "./AxiosInstance";

export const refreshAccessToken = async (): Promise<string | null> => {

  try {
    const response = await api.post(REFRESH_TOKEN_ENDPOINT);
    console.log(response)
    const responseData = response.data.data;

    const {
      accessToken: newAccessToken,
      idToken: newIdToken,
      refreshToken: newRefreshToken,
    } = responseData;

    localStorage.setItem("accessToken", newAccessToken);
    localStorage.setItem("idToken", newIdToken);

    if (newRefreshToken) {
      localStorage.setItem("refreshToken", newRefreshToken);
    }

    return newAccessToken;
  } catch (error) {
    console.error("Failed to refresh token", error);
    return null;
  }
};