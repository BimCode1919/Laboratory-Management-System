import { SERVICE_ENDPOINTS } from "@/api/Endpoints";
import axios from "../../../api/AxiosInstance";
import type { UserProfileDTO } from "../types/user.types";
import { jwtDecode } from "jwt-decode";
import type { BaseResponse } from "@/types/BaseResponse";

interface TokenPayload {
  sub: string;
}

export const getAllUsersApi = async (): Promise<UserProfileDTO[]> => {
  const res = await axios.get(`${SERVICE_ENDPOINTS.AUTH}/users`);
  const users = res.data?.data || [];
  return users.map((u: any) => ({
    ...u,
    id: u.id ? String(u.id) : "", 
  }));
};

export const createUserApi = async (user: UserProfileDTO, roleCode: string) => {
  const token = localStorage.getItem("accessToken");
  if (!token) throw new Error("Không tìm thấy accessToken");

  const decoded = jwtDecode<TokenPayload>(token);
  const createBy = decoded.sub;

  const res = await axios.post(
    `${SERVICE_ENDPOINTS.AUTH}/users?roleCode=${roleCode}&createBy=${createBy}`,
    user
  );
  return res.data?.data;
};

export const getUserByIdApi = async (id: string): Promise<UserProfileDTO> => {
  const res = await axios.get(`${SERVICE_ENDPOINTS.AUTH}/users/${id}`);
  const user = res.data?.data;
  return { ...user, id: String(user.id) };
};

export const updateUserApi = async (id: string, dto: UserProfileDTO) => {
  const res = await axios.put(`${SERVICE_ENDPOINTS.AUTH}/users/${id}`, dto);
  return res.data?.data;
};

export const deleteUserApi = async (id: string) => {
  const res = await axios.delete(`${SERVICE_ENDPOINTS.AUTH}/users/${id}`);
  return res.data?.data;
};

export const restoreUserApi = async (id: string) => {
  const res = await axios.put(`${SERVICE_ENDPOINTS.AUTH}/users/${id}/restore`);
  return res.data?.data;
};

export const getCurrentUser = async (): Promise<UserProfileDTO> => {
  const response = await axios.get<BaseResponse<UserProfileDTO>>(`${SERVICE_ENDPOINTS.AUTH}/users/me`);
  return response.data.data;
}