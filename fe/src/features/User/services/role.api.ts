import { SERVICE_ENDPOINTS } from "@/api/Endpoints";
import axios from "../../../api/AxiosInstance";
import type { RoleListDTO, RoleDTO, RoleUpdateDTO, PrivilegesDTO } from "../types/role.types";
import { jwtDecode } from "jwt-decode";

interface TokenPayload {
  sub: string;
}

export const getAllRoles = async (
  keyword?: string,
  sortBy?: string,
  order?: string
): Promise<RoleListDTO[]> => {
  const token = localStorage.getItem("accessToken");
  if (!token) throw new Error("Không tìm thấy accessToken");

  const params: Record<string, string> = {};
  if (keyword) params.keyword = keyword;
  if (sortBy) params.sortBy = sortBy;
  if (order) params.order = order;

  const res = await axios.get(`${SERVICE_ENDPOINTS.AUTH}/roles`, {
    params,
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  return res.data?.data || [];
};

export const createRole = async (dto: RoleDTO) => {
  const token = localStorage.getItem("accessToken");
  if (!token) throw new Error("Không tìm thấy accessToken");

  const decoded = jwtDecode<TokenPayload>(token);
  const createBy = decoded.sub;

  const res = await axios.post(
    `${SERVICE_ENDPOINTS.AUTH}/roles?createBy=${createBy}`,
    dto,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );
  return res.data?.data;
};

export const updateRole = async (id: string, dto: RoleUpdateDTO) => {
  const token = localStorage.getItem("accessToken");
  if (!token) throw new Error("Không tìm thấy accessToken");

  const decoded = jwtDecode<TokenPayload>(token);
  const updatedBy = decoded.sub;

  const res = await axios.put(
    `${SERVICE_ENDPOINTS.AUTH}/roles/${id}?updatedBy=${updatedBy}`,
    dto,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );
  return res.data?.data;
};

export const deleteRole = async (id: string) => {
  const token = localStorage.getItem("accessToken");
  if (!token) throw new Error("Không tìm thấy accessToken");

  const decoded = jwtDecode<TokenPayload>(token);
  const deletedBy = decoded.sub;

  const res = await axios.delete(
    `${SERVICE_ENDPOINTS.AUTH}/roles/${id}?deletedBy=${deletedBy}`,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );
  return res.data?.data;
};

export const getAllPrivilege = async (): Promise<PrivilegesDTO[]> => {
  const token = localStorage.getItem("accessToken");
  if (!token) throw new Error("Không tìm thấy accessToken");

  const res = await axios.get(`${SERVICE_ENDPOINTS.AUTH}/privilege`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  return res.data?.data || [];
};
