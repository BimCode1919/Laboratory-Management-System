// src/features/Monitoring/service/syncUpRequestsApiService.ts

import axios from "axios";
import api from "../../../api/AxiosInstance";
import { SERVICE_ENDPOINTS } from "../../../api/Endpoints";
import type { BaseResponse } from "@/types/BaseResponse";
import type {
  SyncUpRequests,
  SyncUpRequestsCreateDTO,
  SyncUpRequestsUpdateDTO,
  SyncUpRequestsPaginatedResponse,
  SyncUpRequestsStatus,
} from "../types/SyncUpRequests";

const SYNC_UP_REQUESTS_API_URL = `${SERVICE_ENDPOINTS.MONITORING}/sync-up-requests`;

/**
 * Get all sync up requests with pagination
 */
export const getAllSyncUpRequests = async (
  page: number = 0,
  size: number = 10,
  sortField: string = "createdAt",
  sortOrder: string = "desc"
): Promise<SyncUpRequestsPaginatedResponse> => {
  try {
    const response = await api.get<BaseResponse<SyncUpRequestsPaginatedResponse>>(
      `${SYNC_UP_REQUESTS_API_URL}?page=${page}&size=${size}&sortField=${sortField}&sortOrder=${sortOrder}`
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error("Failed to fetch sync up requests:", error);
    const message = axios.isAxiosError(error)
      ? error.response?.data?.message || "Could not retrieve sync up requests."
      : "Could not retrieve sync up requests.";
    throw new Error(message);
  }
};

/**
 * Get sync up request by id
 */
export const getSyncUpRequestById = async (
  id: string
): Promise<SyncUpRequests> => {
  try {
    const response = await api.get<BaseResponse<SyncUpRequests>>(
      `${SYNC_UP_REQUESTS_API_URL}/${id}`
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error(`Failed to fetch sync up request with ID ${id}:`, error);
    const status = axios.isAxiosError(error) ? error.response?.status : undefined;
    if (status === 404) {
      throw new Error("Sync up request not found.");
    }
    const message = axios.isAxiosError(error)
      ? error.response?.data?.message || "Could not retrieve sync up request."
      : "Could not retrieve sync up request.";
    throw new Error(message);
  }
};

/**
 * Get sync up requests by status
 */
export const getSyncUpRequestsByStatus = async (
  status: SyncUpRequestsStatus,
  page: number = 0,
  size: number = 10
): Promise<SyncUpRequestsPaginatedResponse> => {
  try {
    const response = await api.get<BaseResponse<SyncUpRequestsPaginatedResponse>>(
      `${SYNC_UP_REQUESTS_API_URL}/status/${status}?page=${page}&size=${size}`
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error(
      `Failed to fetch sync up requests with status ${status}:`,
      error
    );
    const statusCode = axios.isAxiosError(error) ? error.response?.status : undefined;
    if (statusCode === 400) {
      throw new Error("Invalid status value.");
    }
    const message = axios.isAxiosError(error)
      ? error.response?.data?.message || "Could not retrieve sync up requests."
      : "Could not retrieve sync up requests.";
    throw new Error(message);
  }
};

/**
 * Create new sync up request
 */
export const createSyncUpRequest = async (
  data: SyncUpRequestsCreateDTO
): Promise<SyncUpRequests> => {
  try {
    const response = await api.post<BaseResponse<SyncUpRequests>>(
      SYNC_UP_REQUESTS_API_URL,
      data
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error("Failed to create sync up request:", error);
    const message = axios.isAxiosError(error)
      ? error.response?.data?.message ||
        "An error occurred while creating the sync up request."
      : "An error occurred while creating the sync up request.";
    throw new Error(message);
  }
};

/**
 * Update sync up request
 */
export const updateSyncUpRequest = async (
  id: string,
  data: SyncUpRequestsUpdateDTO
): Promise<SyncUpRequests> => {
  try {
    const response = await api.put<BaseResponse<SyncUpRequests>>(
      `${SYNC_UP_REQUESTS_API_URL}/${id}`,
      data
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error(`Failed to update sync up request with ID ${id}:`, error);
    const status = axios.isAxiosError(error) ? error.response?.status : undefined;
    if (status === 404) {
      throw new Error("Sync up request not found.");
    }
    const message = axios.isAxiosError(error)
      ? error.response?.data?.message ||
        "An error occurred while updating the sync up request."
      : "An error occurred while updating the sync up request.";
    throw new Error(message);
  }
};

/**
 * Delete sync up request by id
 */
export const deleteSyncUpRequest = async (id: string): Promise<void> => {
  try {
    await api.delete(`${SYNC_UP_REQUESTS_API_URL}/${id}`);
  } catch (error: unknown) {
    console.error(`Failed to delete sync up request with ID ${id}:`, error);
    const status = axios.isAxiosError(error) ? error.response?.status : undefined;
    if (status === 404) {
      throw new Error("Sync up request not found.");
    }
    const message = axios.isAxiosError(error)
      ? error.response?.data?.message ||
        "An error occurred while deleting the sync up request."
      : "An error occurred while deleting the sync up request.";
    throw new Error(message);
  }
};

