// src/features/Monitoring/service/hl7BackupApiService.ts

import axios from "axios";
import api from "../../../api/AxiosInstance";
import { SERVICE_ENDPOINTS } from "../../../api/Endpoints";
import type { BaseResponse } from "@/types/BaseResponse";
import type {
  HL7Backup,
  HL7BackupCreateDTO,
  HL7BackupPaginatedResponse,
} from "../types/HL7Backup";

const HL7_BACKUP_API_URL = `${SERVICE_ENDPOINTS.MONITORING}/hl7-backups`;

/**
 * Get all HL7 backups with pagination
 */
export const getAllHL7Backups = async (
  page: number = 0,
  size: number = 10,
  sortField: string = "createdAt",
  sortOrder: string = "desc"
): Promise<HL7BackupPaginatedResponse> => {
  try {
    const response = await api.get<BaseResponse<HL7BackupPaginatedResponse>>(
      `${HL7_BACKUP_API_URL}?page=${page}&size=${size}&sortField=${sortField}&sortOrder=${sortOrder}`
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error("Failed to fetch HL7 backups:", error);
    const message = axios.isAxiosError(error)
      ? error.response?.data?.message || "Could not retrieve HL7 backups."
      : "Could not retrieve HL7 backups.";
    throw new Error(message);
  }
};

/**
 * Get HL7 backup by id
 */
export const getHL7BackupById = async (id: string): Promise<HL7Backup> => {
  try {
    const response = await api.get<BaseResponse<HL7Backup>>(
      `${HL7_BACKUP_API_URL}/${id}`
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error(`Failed to fetch HL7 backup with ID ${id}:`, error);
    const status = axios.isAxiosError(error) ? error.response?.status : undefined;
    if (status === 404) {
      throw new Error("HL7 backup not found.");
    }
    const message = axios.isAxiosError(error)
      ? error.response?.data?.message || "Could not retrieve HL7 backup."
      : "Could not retrieve HL7 backup.";
    throw new Error(message);
  }
};

/**
 * Get HL7 backups by run ID
 */
export const getHL7BackupsByRunId = async (
  runId: string
): Promise<HL7Backup[]> => {
  try {
    const response = await api.get<BaseResponse<HL7Backup[]>>(
      `${HL7_BACKUP_API_URL}/run/${runId}`
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error(`Failed to fetch HL7 backups for runId ${runId}:`, error);
    const message = axios.isAxiosError(error)
      ? error.response?.data?.message || "Could not retrieve HL7 backups."
      : "Could not retrieve HL7 backups.";
    throw new Error(message);
  }
};

/**
 * Get HL7 backups by instrument ID
 */
export const getHL7BackupsByInstrument = async (
  instrumentId: string,
  page: number = 0,
  size: number = 10
): Promise<HL7BackupPaginatedResponse> => {
  try {
    const response = await api.get<BaseResponse<HL7BackupPaginatedResponse>>(
      `${HL7_BACKUP_API_URL}/instrument/${instrumentId}?page=${page}&size=${size}`
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error(
      `Failed to fetch HL7 backups for instrument ${instrumentId}:`,
      error
    );
    const message = axios.isAxiosError(error)
      ? error.response?.data?.message || "Could not retrieve HL7 backups."
      : "Could not retrieve HL7 backups.";
    throw new Error(message);
  }
};

/**
 * Create new HL7 backup
 */
export const createHL7Backup = async (
  data: HL7BackupCreateDTO
): Promise<HL7Backup> => {
  try {
    const response = await api.post<BaseResponse<HL7Backup>>(
      HL7_BACKUP_API_URL,
      data
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error("Failed to create HL7 backup:", error);
    const message = axios.isAxiosError(error)
      ? error.response?.data?.message ||
        "An error occurred while creating the HL7 backup."
      : "An error occurred while creating the HL7 backup.";
    throw new Error(message);
  }
};

/**
 * Delete HL7 backup by id
 */
export const deleteHL7Backup = async (id: string): Promise<void> => {
  try {
    await api.delete(`${HL7_BACKUP_API_URL}/${id}`);
  } catch (error: unknown) {
    console.error(`Failed to delete HL7 backup with ID ${id}:`, error);
    const status = axios.isAxiosError(error) ? error.response?.status : undefined;
    if (status === 404) {
      throw new Error("HL7 backup not found.");
    }
    const message = axios.isAxiosError(error)
      ? error.response?.data?.message ||
        "An error occurred while deleting the HL7 backup."
      : "An error occurred while deleting the HL7 backup.";
    throw new Error(message);
  }
};

/**
 * Delete HL7 backups by run ID
 */
export const deleteHL7BackupsByRunId = async (runId: string): Promise<number> => {
  try {
    const response = await api.delete<BaseResponse<{ deleted: number }>>(
      `${HL7_BACKUP_API_URL}/run/${runId}`
    );
    // Backend returns message with count, we'll parse it or return a default
    return 0; // Backend doesn't return the count in data, just in message
  } catch (error: unknown) {
    console.error(`Failed to delete HL7 backups for runId ${runId}:`, error);
    const message = axios.isAxiosError(error)
      ? error.response?.data?.message ||
        "An error occurred while deleting HL7 backups."
      : "An error occurred while deleting HL7 backups.";
    throw new Error(message);
  }
};

