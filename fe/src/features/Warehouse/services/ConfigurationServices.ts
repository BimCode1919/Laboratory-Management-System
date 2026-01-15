// src/features/Warehouse/services/ConfigurationServices.ts

import AxiosInstance from "@/api/AxiosInstance";
import { WarehouseEndpoints } from "@/api/Endpoints";
import { jwtDecode } from 'jwt-decode';
import type { CreateConfigurationRequest, ConfigurationDTO } from "../types/configuration";

export const createConfiguration = async (
  req: CreateConfigurationRequest
): Promise<ConfigurationDTO | null> => {
  try {
    const response = await AxiosInstance.post(WarehouseEndpoints.CONFIGURATIONS, req);
    return response.data?.data || response.data || null;
  } catch (err) {
    console.error("Failed to create configuration:", err);
    return null;
  }
};

export const getAllConfigurations = async (): Promise<ConfigurationDTO[] | null> => {
  try {
    const response = await AxiosInstance.get(WarehouseEndpoints.CONFIGURATIONS);
    // Normalize wrapper
    const data = response.data;
    if (data && data.data && Array.isArray(data.data)) return data.data;
    if (Array.isArray(data)) return data;
    if (data && data.content && Array.isArray(data.content)) return data.content;
    return [];
  } catch (err) {
    console.error("Failed to fetch configurations:", err);
    return null;
  }
};

export const getConfigurationById = async (id: string): Promise<ConfigurationDTO | null> => {
  try {
    const response = await AxiosInstance.get(`${WarehouseEndpoints.CONFIGURATIONS}/${id}`);
    const data = response.data;
    return data?.data || data || null;
  } catch (err) {
    console.error("Failed to fetch configuration by id:", err);
    return null;
  }
};

export const updateConfiguration = async (
  id: string,
  req: CreateConfigurationRequest
): Promise<ConfigurationDTO | null> => {
  try {
    // Attach updatedBy query param using the current user's subject from access token when available
    const token = localStorage.getItem('accessToken');
    let url = `${WarehouseEndpoints.CONFIGURATIONS}/${id}`;
    if (token) {
      try {
        // decode to extract 'sub'
        const decoded: any = jwtDecode(token as string);
        const updatedBy = decoded?.sub;
        if (updatedBy) url += `?updatedBy=${updatedBy}`;
      } catch (e) {
        // ignore decode errors and call without updatedBy
        console.warn('Failed to decode token for updatedBy param', e);
      }
    }

    const response = await AxiosInstance.put(url, req);
    const data = response.data;
    return data?.data || data || null;
  } catch (err) {
    console.error("Failed to update configuration:", err);
    return null;
  }
};

export const deleteConfiguration = async (id: string): Promise<boolean> => {
  try {
    await AxiosInstance.delete(`${WarehouseEndpoints.CONFIGURATIONS}/${id}`);
    return true;
  } catch (err) {
    console.error("Failed to delete configuration:", err);
    return false;
  }
};

export const cloneGlobalConfigsToInstrument = async (
  instrumentId: string
): Promise<ConfigurationDTO[] | null> => {
  try {
    const response = await AxiosInstance.post(WarehouseEndpoints.CLONE_CONFIGS(instrumentId));
    const data = response.data;
    if (data && data.data && Array.isArray(data.data)) return data.data;
    if (Array.isArray(data)) return data;
    if (data && data.content && Array.isArray(data.content)) return data.content;
    return [];
  } catch (err) {
    console.error("Failed to clone configurations:", err);
    return null;
  }
};
