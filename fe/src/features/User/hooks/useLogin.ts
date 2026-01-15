// src/features/User/hooks/useLogin.ts

import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { jwtDecode } from "jwt-decode";
import {
  loginApi,
  loginWithGoogle,
  verifyGoogleOtp,
} from "../services/auth.api";
import type { LoginDTO } from "../types/auth.types";
import { confirmFirstLoginApi } from "../services/firstLogin.api";
import { useAuth } from "@/context/AuthContext";
import type { UserProfileDTO } from "../types/user.types";
import { getCurrentUser } from "../services/user.api";

interface DecodedToken {
  sub: string;
  exp?: number;
  ["cognito:groups"]?: string[];
}

const ROLE_REDIRECT_MAP: Record<string, string> = {
  ADMIN: "/admin",
  LAB_USER: "/patients/list",
  PATIENT: "/patients/me",
};

export const useLogin = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();
  const [firstLogin, setFirstLogin] = useState(false);
  const [session, setSession] = useState<string | null>(null);
  const { setRole, setUser } = useAuth();

  const handleLogin = async (values: LoginDTO) => {
    try {
      setLoading(true);
      setError(null);

      const data = await loginApi(values);

      if (data.firstLogin) {
        setFirstLogin(true);
        setSession(data.session);
        localStorage.setItem("firstLoginSession", data.session);
        return;
      }

      localStorage.setItem("accessToken", data.accessToken);
      localStorage.setItem("idToken", data.idToken);
      localStorage.setItem("deviceId", data.deviceId);

      const decoded = jwtDecode<DecodedToken>(data.accessToken);
      const groups = decoded["cognito:groups"] || [];
      console.log("Groups: ", groups);
      const sub: string = decoded.sub;
      localStorage.setItem("sub", sub);

      const primaryRole = groups[0]?.toUpperCase() || "PATIENT";
      const redirectPath = ROLE_REDIRECT_MAP[primaryRole] || "/";
      console.log("Primary Role: ", primaryRole);
      setRole(primaryRole);

      const userData: UserProfileDTO = await handleFetchCurrentUserData();
      setUser(userData);

      navigate(redirectPath);
    } catch (err: any) {
      setError(err.response?.data?.message || "Fail to login");
    } finally {
      setLoading(false);
    }
  };

  const handleFirstLogin = async (
    identifyNumber: string,
    newPassword: string
  ) => {
    try {
      const session = localStorage.getItem("firstLoginSession");
      if (!session) throw new Error("Session not found.");
      setLoading(true);
      setError(null);

      await confirmFirstLoginApi({ identifyNumber, newPassword, session });
      alert("Successfully change password. Please login again.");

      setFirstLogin(false);
      setSession(null);
    } catch (err: any) {
      setError(err.response?.data?.message || "Fail to change password.");
    } finally {
      setLoading(false);
    }
  };

  const handleFetchCurrentUserData = async () => {
    const response = await getCurrentUser();
    return response;
  };

  const handleGoogleLogin = async (code: string) => {
    try {
      setLoading(true);
      const message = await loginWithGoogle(code);
      alert(message);
    } catch (err: any) {
      setError(err.response?.data?.message || "Google login failed.");
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyGoogleOtp = async (email: string, otp: string) => {
    try {
      setLoading(true);
      const data = await verifyGoogleOtp(email, otp);

      localStorage.setItem("accessToken", data.accessToken);
      localStorage.setItem("idToken", data.idToken);

      const decoded = jwtDecode<DecodedToken>(data.accessToken);
      const groups = decoded["cognito:groups"] || [];
      console.log("Groups: ", groups);
      const sub: string = decoded.sub;
      localStorage.setItem("sub", sub);

      const primaryRole = groups[0]?.toUpperCase() || "PATIENT";
      console.log("Primary Role: ", primaryRole);
      setRole(primaryRole);

      const userData: UserProfileDTO = await handleFetchCurrentUserData();
      setUser(userData);

      navigate(ROLE_REDIRECT_MAP[primaryRole] || "/");
    } catch (err: any) {
      setError(err.response?.data?.message || "OTP verification failed.");
    } finally {
      setLoading(false);
    }
  };

  return {
    handleLogin,
    handleFirstLogin,
    handleGoogleLogin,
    handleVerifyGoogleOtp,
    loading,
    error,
    firstLogin,
  };
};
