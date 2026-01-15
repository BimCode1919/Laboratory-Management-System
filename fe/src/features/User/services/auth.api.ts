import axiosInstance from "@/api/AxiosInstance";
import type { LoginDTO, JwtTokenDTO } from "../types/auth.types";
import type { BaseResponse } from "@/types/BaseResponse";

export const loginApi = async (payload: LoginDTO): Promise<JwtTokenDTO> => {
  const response = await axiosInstance.post<BaseResponse<JwtTokenDTO>>("/iam-service/login", payload);
  return response.data.data;
};
